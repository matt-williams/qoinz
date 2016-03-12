package com.github.matt.williams.qoinz.webapp;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.realexpayments.hpp.sdk.RealexHpp;
import com.realexpayments.hpp.sdk.domain.HppResponse;

public class ResponseConsumerServlet extends HppServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		/*
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = req.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);				
			}
		} catch (Exception e) {
		}

		//String responseJson = sb.toString();
	    */
		String responseJson = req.getParameter("hppResponse");
		System.out.println("Got " + responseJson);
		RealexHpp realexHpp = new RealexHpp("secret");
		HppResponse hppResponse = realexHpp.responseFromJson(responseJson);
		String result = hppResponse.getResult();
		//String message = hppResponse.getMessage();
		//String authCode = hppResponse.getAuthCode();
		rsp.getOutputStream().print("{\"result\":\"" + result + "\"}");
	}
	
	
}
