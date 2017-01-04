package com.sandbox.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericResponse {

	private String resultCode;

	private String resultMessage;

	private JsonNode resultData;

	public JsonNode getResultData() {
		return resultData;
	}

	public void setResultData(JsonNode resultData) {
		this.resultData = resultData;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
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

}
