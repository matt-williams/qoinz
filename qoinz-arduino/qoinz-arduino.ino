/*
 * MFRC522 typical pin layout used:
 * -----------------------------------------------------------------------------------------
 *             MFRC522      Arduino       Arduino   Arduino    Arduino          Arduino
 *             Reader/PCD   Uno           Mega      Nano v3    Leonardo/Micro   Pro Micro
 * Signal      Pin          Pin           Pin       Pin        Pin              Pin
 * -----------------------------------------------------------------------------------------
 * RST/Reset   RST          9             5         D9         RESET/ICSP-5     RST
 * SPI SS      SDA(SS)      10            53        D10        10               10
 * SPI MOSI    MOSI         11 / ICSP-4   51        D11        ICSP-4           16
 * SPI MISO    MISO         12 / ICSP-1   50        D12        ICSP-1           14
 * SPI SCK     SCK          13 / ICSP-3   52        D13        ICSP-3           15
 */

#include <SPI.h>
#include <MFRC522.h>
#include <Adafruit_NeoPixel.h>

#define NEOPIXEL_PIN    2
#define COIN_MECH_PIN1  3
#define COIN_MECH_PIN2  4
#define RST_PIN         9          // Configurable, see typical pin layout above
#define SS_PIN          10         // Configurable, see typical pin layout above

MFRC522 mfrc522(SS_PIN, RST_PIN);  // Create MFRC522 instance
Adafruit_NeoPixel strip = Adafruit_NeoPixel(12, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ400);

#define STRIP_STATE_IDLE 0
#define STRIP_STATE_COMMUNICATING 1
#define STRIP_STATE_QOIN_ACCEPTED 2
#define STRIP_STATE_COIN_ACCEPTED 3
#define STRIP_STATE_ERROR 4
byte strip_state;
unsigned short strip_idle_countdown;
unsigned short strip_cycle;

void setup() {
	Serial.begin(9600);		// Initialize serial communications with the PC
	SPI.begin();			// Init SPI bus
	mfrc522.PCD_Init();		// Init MFRC522
	mfrc522.PCD_DumpVersionToSerial();	// Show details of PCD - MFRC522 Card Reader details

  pinMode(COIN_MECH_PIN1, INPUT_PULLUP);
  pinMode(COIN_MECH_PIN2, INPUT_PULLUP);

  strip.begin();
  strip.setBrightness(255);
  strip_state = STRIP_STATE_IDLE;
  strip_idle_countdown = 0;
  strip_cycle = 0;
  strip_update();
  strip.show(); // Initialize all pixels to 'off'  
}

void strip_set_state(char state) {
  strip_state = state;
  if (strip_state != STRIP_STATE_IDLE) {
    strip_idle_countdown = 0x300;
  } else {
    strip_idle_countdown = 0;
  }
}

void strip_update() {
  for (byte i = 0; i < strip.numPixels(); i++) {
    strip.setPixelColor(i, 0, 0, 0);
  }
  byte i1 = strip_cycle >> 12;
  byte j2 = (strip_cycle >> 4) & 0xff;
  byte i2 = (i1 + 1) % strip.numPixels();
  byte j1 = 255 - j2;
  switch (strip_state) {
    case STRIP_STATE_IDLE:
      /*
      strip.setPixelColor(i1, 0, j1 >> 4, j1 >> 5);
      strip.setPixelColor(i2, 0, j2 >> 4, j2 >> 5);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), 0, j1 >> 4, j1 >> 5);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), 0, j2 >> 4, j2 >> 5);
      strip_cycle += 8;
      */
      break;
    case STRIP_STATE_COMMUNICATING:
      strip.setPixelColor(i1, j1 >> 4, j1 >> 4, 0);
      strip.setPixelColor(i2, j2 >> 4, j2 >> 4, 0);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), j2 >> 4, j1 >> 4, 0);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), j2 >> 4, j2 >> 4, 0);
      strip_cycle += 1024;
      break;
    case STRIP_STATE_QOIN_ACCEPTED:
      strip.setPixelColor(i1, 0, 0, j1 >> 3);
      strip.setPixelColor(i2, 0, 0, j2 >> 3);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), 0, 0, j1 >> 3);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), 0, 0, j2 >> 3);
      strip_cycle += 64;
      break;
    case STRIP_STATE_COIN_ACCEPTED:
      strip.setPixelColor(i1, 0, j1 >> 3, 0);
      strip.setPixelColor(i2, 0, j2 >> 3, 0);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), 0, j1 >> 3, 0);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), 0, j2 >> 3, 0);
      strip_cycle += 64;
      break;
    case STRIP_STATE_ERROR:
      strip.setPixelColor(i1, j1 >> 3, 0, 0);
      strip.setPixelColor(i2, j2 >> 3, 0, 0);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), j1 >> 3, 0, 0);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), j2 >> 3, 0, 0);
      strip_cycle += 64;
      break;
  }
  if (strip_cycle > strip.numPixels() << 12) {
    strip_cycle = 0;
  }
  if (strip_idle_countdown > 0) {
    strip_idle_countdown--;
    if (strip_idle_countdown == 0) {
      strip_state = STRIP_STATE_IDLE;
    }
  }
}

byte oldCoinMechPin1 = 1;
byte oldCoinMechPin2 = 1;
unsigned int cycles = 0;

void checkForCoin() {
  cycles++;
  
  byte coinMechPin1 = digitalRead(COIN_MECH_PIN1);
  byte coinMechPin2 = digitalRead(COIN_MECH_PIN2);
  if ((oldCoinMechPin1 == 0) && (coinMechPin1 == 1) &&
      (oldCoinMechPin2 == 1) && (coinMechPin2 == 1)) {
    cycles = 0;    
  } else if ((oldCoinMechPin1 == 1) && (coinMechPin1 == 1) &&
             (oldCoinMechPin2 == 1) && (coinMechPin2 == 0) &&
             (cycles <= 10)) {
    Serial.print(cycles);
    Serial.println(F(" coin"));
    strip_set_state(STRIP_STATE_COIN_ACCEPTED);
    while (strip_state != STRIP_STATE_IDLE) {
      strip_update();
      strip.show();
    } 
    strip_update();
    strip.show();
  }
  oldCoinMechPin1 = coinMechPin1;
  oldCoinMechPin2 = coinMechPin2;
  /*
  if (coinMechPin1 != oldCoinMechPin1) {
    Serial.print(cycles);
    Serial.print(F(" COIN_MECH_PIN1: "));
    Serial.print(oldCoinMechPin1);
    Serial.print(F(" => "));
    Serial.print(coinMechPin1);
    Serial.println();
    oldCoinMechPin1 = coinMechPin1;
    cycles = 0;
  }
 
  if (coinMechPin2 != oldCoinMechPin2) {
    Serial.print(cycles);
    Serial.print(F(" COIN_MECH_PIN2: "));
    Serial.print(oldCoinMechPin2);
    Serial.print(F(" => "));
    Serial.print(coinMechPin2);
    Serial.println();
    oldCoinMechPin2 = coinMechPin2;
    cycles = 0;
  }
  */
}

void print_hex(const __FlashStringHelper* label, byte* data, byte data_len) {
  Serial.print(label);
  Serial.print(':');
  for (byte i = 0; i < data_len; i++) {
     if(data[i] < 0x10)
       Serial.print(F(" 0"));
     else
       Serial.print(F(" "));
     Serial.print(data[i], HEX);
  }
  Serial.println();
}

void strip_show_error() {
  for (byte i = 0; i < strip.numPixels(); i++) {
    strip.setPixelColor(i, 63, 0, 0);
  }
  strip.show();
}

byte nfc_step = 0;
unsigned short nfcCountdown = 0;
void checkForQoin() {
  if (nfcCountdown > 0) {
    nfcCountdown--;
    return;
  }
  nfcCountdown = 1000;
  
  byte back[32];
  byte back_len = 32;
  MFRC522::StatusCode sc;
  
  if (nfc_step == 0) {
    for (byte i = 0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, 0, 0, 0);
    }
    strip.show();
    
    // Look for new cards
    if (!mfrc522.PICC_IsNewCardPresent()) {
      return;
    }

    // Select one of the cards
    if (!mfrc522.PICC_ReadCardSerial()) {
      return;
    }
    nfc_step = 1;
  }

  if (nfc_step == 1) {
    byte apdu[] = {0xe0, 0x00, 0x00, 0x00};
    byte apdu_len = 2;
    // Returns 05 78 80 70 02 A5 46C
    mfrc522.PCD_CalculateCRC(apdu, apdu_len, &apdu[apdu_len]);
    if ((sc = mfrc522.PCD_TransceiveData(apdu, apdu_len + 2, back, &back_len, NULL, 0, true)) != 0) {
      Serial.println(sc);
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    print_hex(F("RATS"), back, back_len);
    nfc_step = 2;
    return;
  }

  if (nfc_step == 2) {
    byte apdu2[] = {0x0a, 0x00, 0X00, 0xA4, 0x04, 0x00, 0x07, 0xF0, 0x4d, 0x4a, 0x54, 0x54, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte apdu2_len = 15;
    // Returns 0A 00 6A 82 91 B5
    mfrc522.PCD_CalculateCRC(apdu2, apdu2_len, &apdu2[apdu2_len]);
    if ((sc = mfrc522.PCD_TransceiveData(apdu2, apdu2_len + 2, back, &back_len, NULL, 0, true)) != 0) {
      Serial.println(sc);
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    print_hex(F("APDU"), back, back_len);
    if ((back[2] != 0x90) || (back[3] != 0x00)) {
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    nfc_step = 3;
    return;
  }

  if (nfc_step == 3) {
    byte apdu3[] = {0x0b, 0x00, 0XD0, 0x01, 0x00, 0x00, 0x05, 0x00, 0x00};
    byte apdu3_len = 7;
    // Returns 0A 00 6A 82 91 B5
    mfrc522.PCD_CalculateCRC(apdu3, apdu3_len, &apdu3[apdu3_len]);
    if ((sc = mfrc522.PCD_TransceiveData(apdu3, apdu3_len + 2, back, &back_len, NULL, 0, true)) != 0) {
      Serial.println(sc);
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    print_hex(F("APDU2"), back, back_len);
    if ((back[2] != 0x90) || (back[3] != 0x00)) {
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    nfc_step = 4;
    return;
  }

  if (nfc_step == 4) {
    byte apdu3[] = {0x0a, 0x00, 0XD0, 0x02, 0x00, 0x00, 0x00, 0x00};
    byte apdu3_len = 6;
    // Returns 0A 00 6A 82 91 B5
    mfrc522.PCD_CalculateCRC(apdu3, apdu3_len, &apdu3[apdu3_len]);
    if ((sc = mfrc522.PCD_TransceiveData(apdu3, apdu3_len + 2, back, &back_len, NULL, 0, true)) != 0) {
      Serial.println(sc);
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    print_hex(F("APDU3"), back, back_len);
    if ((back[2] == 0x90) && (back[3] == 0x01)) {
      mfrc522.PICC_HaltA();
      nfc_step = 6;
    } else if ((back[2] != 0x90) || (back[3] != 0x00)) {
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    } else {
      nfc_step = 5;
      nfcCountdown = 60000;
      strip_cycle = strip_cycle &~ 1023;
      strip_set_state(STRIP_STATE_COMMUNICATING);
      strip_update();
      strip.show();
      return;
    }
  }

  if (nfc_step == 5) {
    byte apdu3[] = {0x0b, 0x00, 0XD0, 0x02, 0x00, 0x00, 0x00, 0x00};
    byte apdu3_len = 6;
    mfrc522.PCD_CalculateCRC(apdu3, apdu3_len, &apdu3[apdu3_len]);
    if ((sc = mfrc522.PCD_TransceiveData(apdu3, apdu3_len + 2, back, &back_len, NULL, 0, true)) != 0) {
      Serial.println(sc);
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    }
    print_hex(F("APDU4"), back, back_len);
    if ((back[2] == 0x90) && (back[3] == 0x01)) {
      mfrc522.PICC_HaltA();
      nfc_step = 6;
    } else if ((back[2] != 0x90) || (back[3] != 0x00)) {
      strip_show_error();
      mfrc522.PICC_HaltA();
      nfc_step = 0;
      return;
    } else {
      nfc_step = 4;
      nfcCountdown = 60000;
      strip_cycle = strip_cycle &~ 1023;
      strip_set_state(STRIP_STATE_COMMUNICATING);
      strip_update();
      strip.show();
      return;
    }
  }

  if (nfc_step == 6) {
    mfrc522.PICC_HaltA();
    nfc_step = 0;
    strip_set_state(STRIP_STATE_QOIN_ACCEPTED);
    while (strip_state != STRIP_STATE_IDLE) {
      strip_update();
      strip.show();
    } 
    strip_update();
    strip.show();
    nfcCountdown = 60000;
    return;
  }
}

void loop() {
  checkForCoin();
  checkForQoin();
}
