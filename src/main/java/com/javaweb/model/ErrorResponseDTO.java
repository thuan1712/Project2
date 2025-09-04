package com.javaweb.model;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponseDTO {
	 private String error;
	 private List<String> detal = new ArrayList<>();
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public List<String> getDetal() {
		return detal;
	}
	public void setDetal(List<String> detal) {
		this.detal = detal;
	}
     
}