package com.github.matt.williams.qoinz.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.realexpayments.hpp.sdk.RealexHpp;
import com.realexpayments.hpp.sdk.domain.HppResponse;

public class ResponseConsumerServlet extends HppServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		String responseJson = req.getParameter("hppResponse");
		RealexHpp realexHpp = new RealexHpp(mHppSecret);
		HppResponse hppResponse = realexHpp.responseFromJson(responseJson);
		String result = hppResponse.getResult();
		String numQoinzStr = hppResponse.getSupplementaryData().get("numQoinz");
		String id = hppResponse.getSupplementaryData().get("qoinzId");
		if ((numQoinzStr != null) && (id != null)) {
			int numQoinz = Integer.parseInt(numQoinzStr);
			QoinsDatabase.getInstance().add(id, numQoinz);
		}
		//String message = hppResponse.getMessage();
		//String authCode = hppResponse.getAuthCode();
		rsp.getOutputStream().print("{\"result\":\"" + result + "\"}");
	}	
}
