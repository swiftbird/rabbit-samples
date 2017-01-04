package com.sandbox.simplemains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTestSLF {

	public static void main(String[] args) {

		Logger log = LoggerFactory.getLogger(LoggerTestSLF.class);
		
		System.out.println("Log level is: " + log.isTraceEnabled());
		
		log.info("This should be printed");
		log.trace(" This should not " + LoggerTestSLF.makeACall());

	}
	
	public static String makeACall() {
		System.out.println("Make a call was called");
		return "Made a call";
	}

}
