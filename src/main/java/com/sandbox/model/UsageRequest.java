package com.sandbox.model;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UsageRequest {

	private String msisdn;
	private String requestId;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String toJSON() {
		String result = "";
		ObjectMapper mapper = new ObjectMapper();

		try {

			String jsonStat = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
			result = jsonStat;

		} catch (Exception e) {

			e.printStackTrace();

		}

		return result;

	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
}
