package com.github.matt.williams.qoinz.webapp;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

public class HppServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String HPP_MERCHANT_ID_PARAM_NAME = "hppMerchantId";
	private static final String HPP_SECRET_PARAM_NAME = "hppSecret";

	protected String mHppMerchantId;
	protected String mHppSecret;

	@Override
	public void init(ServletConfig cfg) {
		mHppMerchantId = cfg.getServletContext().getInitParameter(HPP_MERCHANT_ID_PARAM_NAME);
		mHppSecret = cfg.getServletContext().getInitParameter(HPP_SECRET_PARAM_NAME);
	}	
}
