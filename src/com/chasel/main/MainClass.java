package com.chasel.main;

import com.chasel.init.InitContainer;
import com.chasel.service.UserServiceImpl;

public class MainClass {

	/**
	 * 不使用启动类解析，直接new
	 * 必然会抛空指针异常
	 * @param args
	 */
//	public static void main(String[] args) {
//		UserServiceImpl user = new UserServiceImpl();
//		user.getData();
//	}
	
	/**
	 * 启动容器来获取类对象
	 * @param args
	 */
	public static void main(String[] args) {
		InitContainer contain = new InitContainer();
//		System.out.println(contain.getClass().isInterface());
//		UserServiceImpl user = (UserServiceImpl) contain.getBean("com.chasel.service.UserServiceImpl");
//		user.getData();
	}
}
