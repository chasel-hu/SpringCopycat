package com.chasel.main;

import com.chasel.init.InitContainer;
import com.chasel.service.UserServiceImpl;

public class MainClass {

	/**
	 * ��ʹ�������������ֱ��new
	 * ��Ȼ���׿�ָ���쳣
	 * @param args
	 */
//	public static void main(String[] args) {
//		UserServiceImpl user = new UserServiceImpl();
//		user.getData();
//	}
	
	/**
	 * ������������ȡ�����
	 * @param args
	 */
	public static void main(String[] args) {
		InitContainer contain = new InitContainer();
//		System.out.println(contain.getClass().isInterface());
//		UserServiceImpl user = (UserServiceImpl) contain.getBean("com.chasel.service.UserServiceImpl");
//		user.getData();
	}
}
