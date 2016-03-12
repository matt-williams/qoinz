package com.github.matt.williams.qoinz.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.realexpayments.hpp.sdk.RealexHpp;
import com.realexpayments.hpp.sdk.domain.HppRequest;

public class RequestProducerServlet extends HppServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		HppRequest hppRequest = new HppRequest()
		.addAmount(100)
		.addCurrency("GBP")
		.addMerchantId(mHppMerchantId)
		.addAutoSettleFlag(true);

		RealexHpp realexHpp = new RealexHpp(mHppSecret);
		String requestJson = realexHpp.requestToJson(hppRequest);
		rsp.getOutputStream().print(requestJson);
	}
}
