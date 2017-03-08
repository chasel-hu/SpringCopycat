package com.chasel.dao;

public class UserDaoImpl implements IUserDao{

	@Override
	public void setData(String data) {
		System.out.println("data is : " + data);
	}

	@Override
	public String getData() {
		return "just test";
	}

}
