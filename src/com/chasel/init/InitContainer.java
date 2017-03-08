package com.chasel.init;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.chasel.annotation.FieldInject;
import com.chasel.dao.UserDaoImpl;
import com.chasel.service.UserServiceImpl;

public class InitContainer {
	private List<Class> clazzList;
	private List objList;
	
	public InitContainer(){
		clazzList = new ArrayList<Class>();
		objList = new ArrayList();
		
		scanPackage();
		try {
			initClass();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 模拟Spring扫描包
	 * 这里实现比较简单，不会讲各个类进行分类
	 */
	private void scanPackage(){
		try {
			clazzList.add(Class.forName("com.chasel.service.UserServiceImpl"));
			clazzList.add(Class.forName("com.chasel.dao.IUserDao"));
			clazzList.add(Class.forName("com.chasel.dao.UserDaoImpl"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化所有类
	 * @param clazz
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void initClass() throws InstantiationException, IllegalAccessException{
		for(int j=0; j<clazzList.size(); j++){
			Class clazz = clazzList.get(j);
			//去除接口和抽象类，因为它们是不能实例化的
			if(clazz.isInterface()) continue;
			boolean isAbs = Modifier.isAbstract(clazz.getModifiers());
			if(isAbs) continue;
			
			Object target = getBean(clazz.getName());
			if(target != null) continue; //如果已经存在就跳过
			
			Field[] field = clazz.getDeclaredFields();
			Method[] method = clazz.getDeclaredMethods();
			
			Object obj = clazz.newInstance();
			objList.add(obj);
			
			//下面开始注入
			for(int i=0; i<field.length; i++){
				FieldInject annotation = field[i].getAnnotation(FieldInject.class);
				if(annotation != null){//说明这个成员变量有注解
					String fullClassName = field[i].getType().getName(); //获取到成员变量的全限定名
					Class sub = findSubClass(fullClassName);
					if(sub == null) continue; //找不到，略过
					String lowCase = "set"+field[i].getName();
					
					//采用setter方法幅值，与下面的代码二选一即可
//					injectMethod(method,obj, sub, lowCase.toLowerCase()); 
					
					//以下是直接幅值
					injectField(field[i],obj,sub);
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param field
	 * @param target
	 * @param inject
	 */
	private void injectField(Field field, Object target, Class inject){
		//采用setter方法幅值，与下面的代码二选一即可
		Object obj = getBean(inject.getName());
		if(obj == null) {
			try {
				obj = inject.newInstance();
				objList.add(obj);
				
				field.setAccessible(true);
				field.set(target, obj);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 为对象target注入一个成员变量【clazz是其成员变量的子类】
	 * 需要原来的类中提供一个setter方法
	 * @param method
	 * @param target
	 * @param clazz
	 * @param lowCase
	 */
	private void injectMethod(Method[] method,Object target, Class clazz, String lowCase){
		for(int i=0; i<method.length; i++){
			if(method[i].getName().toLowerCase().equals(lowCase)){
				Object obj = getBean(clazz.getName());
				if(obj == null) {
					try {
						obj = clazz.newInstance();
						objList.add(obj);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					method[i].invoke(target, obj);
					return;
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//找到某个类的子类【涉及到Spring的选择策略】
	private Class findSubClass(String fullClassName){
		try {
			Class target = Class.forName(fullClassName);
			
			//不是抽象类，不是接口，那自身就好了。
			if(!target.isInterface()){
				boolean isAbs = Modifier.isAbstract(target.getModifiers());
				if(!isAbs) return target;
			}
			
			int size = clazzList.size();
			for(int i=0; i<size; i++){
				Class p = clazzList.get(i);
				if(p.getSuperclass() != null && p.getSuperclass().getName().equals(fullClassName)) return p;
				Class[] inter = p.getInterfaces();
				for(Class c : inter){
					if(c.getName().equals(fullClassName)) return p;
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 根据全限定名获取类对象
	 * 默认是单例对象，只从本地查找
	 * @param fullClassName
	 * @return
	 */
	public Object getBean(String fullClassName){
		int size = objList.size();
		for(int i=0; i<size; i++){
			if(objList.get(i).getClass().getName().equals(fullClassName)) return objList.get(i);
		}
		return null;
	}
	
	/**
	 * 根据全限定名获取类对象
	 * 如果新建为true，则重新生成一个
	 * @param fullClassName
	 * @param isNewOne
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object getBean(String fullClassName, boolean isNewOne) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		if(!isNewOne) return getBean(fullClassName);
		Class clazz = Class.forName(fullClassName);
		return clazz.newInstance();
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitContainer init = new InitContainer();
		UserServiceImpl user = (UserServiceImpl) init.getBean("com.chasel.service.UserServiceImpl");
		user.getData();
	}
}
