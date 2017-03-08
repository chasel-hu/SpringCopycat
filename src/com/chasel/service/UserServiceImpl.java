package com.chasel.service;

import com.chasel.annotation.FieldInject;
import com.chasel.dao.IUserDao;

public class UserServiceImpl {
	
	@FieldInject
	private IUserDao dao;

	public IUserDao getDao() {
		return dao;
	}

	public void setDao(IUserDao dao) {
		this.dao = dao;
	}
	
	public void setData(){
		this.dao.setData("test");
	}
	
	public void getData(){
		String data = this.dao.getData();
		System.out.println("result is : " + data);
	}
}
