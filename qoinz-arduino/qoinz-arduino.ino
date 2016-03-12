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
      strip.setPixelColor(i1, 0, j1 >> 4, j1 >> 5);
      strip.setPixelColor(i2, 0, j2 >> 4, j2 >> 5);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), 0, j1 >> 4, j1 >> 5);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), 0, j2 >> 4, j2 >> 5);
      strip_cycle += 8;
      break;
    case STRIP_STATE_COMMUNICATING:
      strip.setPixelColor(i1, j1 >> 4, j1 >> 4, 0);
      strip.setPixelColor(i2, j2 >> 4, j2 >> 4, 0);
      strip.setPixelColor((i1 + strip.numPixels() / 2) % strip.numPixels(), j2 >> 4, j1 >> 4, 0);
      strip.setPixelColor((i2 + strip.numPixels() / 2) % strip.numPixels(), j2 >> 4, j2 >> 4, 0);
      strip_cycle += 64;
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

void loop() {
  strip_update();
  strip.show();
  return;
	// Look for new cards
	if ( ! mfrc522.PICC_IsNewCardPresent()) {
		return;
	}

	// Select one of the cards
	if ( ! mfrc522.PICC_ReadCardSerial()) {
		return;
	}

	// Dump debug info about the card; PICC_HaltA() is automatically called
	mfrc522.PICC_DumpToSerial(&(mfrc522.uid));
}
