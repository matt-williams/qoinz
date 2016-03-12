package com.github.matt.williams.qoinz.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetTokenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		String id = req.getParameter("id");
		String challenge = req.getParameter("challenge");
		if ((id != null) && (challenge != null) && (QoinsDatabase.getInstance().decrement(id))) {
			rsp.getOutputStream().print("H(" + challenge + ")");
		} else {
			rsp.sendError(400);
		}
	}	
}
