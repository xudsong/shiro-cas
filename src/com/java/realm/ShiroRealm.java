package com.java.realm;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.java.dao.UserDao;
import com.java.entity.User;
import com.java.util.DbUtil;


public class ShiroRealm extends CasRealm{
	private UserDao userDao=new UserDao();
	private DbUtil dbUtil=new DbUtil();

	protected final Map<String, SimpleAuthorizationInfo> roles = new ConcurrentHashMap<String, SimpleAuthorizationInfo>();
	
	/**
	 * 设置角色和权限信息
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		String userName = (String) principals.getPrimaryPrincipal();
		SimpleAuthorizationInfo authorizationInfo = null;
		Connection con=null;
		if (authorizationInfo == null) {
			authorizationInfo = new SimpleAuthorizationInfo();
			try {
				con=dbUtil.getCon();
			    authorizationInfo.addStringPermissions(userDao.getPermissions(con, userName));
			    authorizationInfo.addRoles(userDao.getRoles(con, userName));
			    roles.put(userName, authorizationInfo);
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try {
					dbUtil.closeCon(con);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return authorizationInfo;
	}
	
	
	/**
	 * 1、CAS认证 ,验证用户身份
	 * 2、将用户基本信息设置到会话中
	 */
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {

		AuthenticationInfo authc = super.doGetAuthenticationInfo(token);
		String userName = (String) authc.getPrincipals().getPrimaryPrincipal();
		Connection con=null;
		
		try{
			con=dbUtil.getCon();
			User user=userDao.getByUserName(con, userName);
			if(user!=null){
				SecurityUtils.getSubject().getSession().setAttribute("user", user);
				//AuthenticationInfo authcInfo=new SimpleAuthenticationInfo(user.getUserName(),user.getPassWord(),"xx");
				return authc;
			}else{
				return null;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;		
	}
}
