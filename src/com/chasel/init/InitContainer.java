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
	 * ģ��Springɨ���
	 * ����ʵ�ֱȽϼ򵥣����ὲ��������з���
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
	 * ��ʼ��������
	 * @param clazz
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void initClass() throws InstantiationException, IllegalAccessException{
		for(int j=0; j<clazzList.size(); j++){
			Class clazz = clazzList.get(j);
			//ȥ���ӿںͳ����࣬��Ϊ�����ǲ���ʵ������
			if(clazz.isInterface()) continue;
			boolean isAbs = Modifier.isAbstract(clazz.getModifiers());
			if(isAbs) continue;
			
			Object target = getBean(clazz.getName());
			if(target != null) continue; //����Ѿ����ھ�����
			
			Field[] field = clazz.getDeclaredFields();
			Method[] method = clazz.getDeclaredMethods();
			
			Object obj = clazz.newInstance();
			objList.add(obj);
			
			//���濪ʼע��
			for(int i=0; i<field.length; i++){
				FieldInject annotation = field[i].getAnnotation(FieldInject.class);
				if(annotation != null){//˵�������Ա������ע��
					String fullClassName = field[i].getType().getName(); //��ȡ����Ա������ȫ�޶���
					Class sub = findSubClass(fullClassName);
					if(sub == null) continue; //�Ҳ������Թ�
					String lowCase = "set"+field[i].getName();
					
					//����setter������ֵ��������Ĵ����ѡһ����
//					injectMethod(method,obj, sub, lowCase.toLowerCase()); 
					
					//������ֱ�ӷ�ֵ
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
		//����setter������ֵ��������Ĵ����ѡһ����
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
	 * Ϊ����targetע��һ����Ա������clazz�����Ա���������ࡿ
	 * ��Ҫԭ���������ṩһ��setter����
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
	
	//�ҵ�ĳ��������ࡾ�漰��Spring��ѡ����ԡ�
	private Class findSubClass(String fullClassName){
		try {
			Class target = Class.forName(fullClassName);
			
			//���ǳ����࣬���ǽӿڣ�������ͺ��ˡ�
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
	 * ����ȫ�޶�����ȡ�����
	 * Ĭ���ǵ�������ֻ�ӱ��ز���
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
	 * ����ȫ�޶�����ȡ�����
	 * ����½�Ϊtrue������������һ��
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
