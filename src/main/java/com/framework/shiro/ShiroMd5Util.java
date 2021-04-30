package com.framework.shiro;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class ShiroMd5Util {
	//添加user的密码加密方法
	public static String sysMd5(String password, String saltString) {
		String hashAlgorithmName = "MD5";//加密方式

		Object crdentials =password;//密码原值

		ByteSource salt = ByteSource.Util.bytes(saltString);//以账号作为盐值

		int hashIterations = 1;//加密1024次

		SimpleHash hash = new SimpleHash(hashAlgorithmName,crdentials,salt,hashIterations);

		return hash.toString();
	}

	public static void main(String[] args) {
		String password = sysMd5("123456", "an");
		System.out.println(password);
	}
}
