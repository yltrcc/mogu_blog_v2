package com.moxi.mogublog.admin.restapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moxi.mogublog.admin.global.SQLConf;
import com.moxi.mogublog.admin.global.SysConf;
import com.moxi.mogublog.config.jwt.Audience;
import com.moxi.mogublog.config.jwt.JwtHelper;
import com.moxi.mogublog.utils.CheckUtils;
import com.moxi.mogublog.utils.ResultUtil;
import com.moxi.mogublog.utils.StringUtils;
import com.moxi.mogublog.xo.entity.Admin;
import com.moxi.mogublog.xo.entity.AdminRole;
import com.moxi.mogublog.xo.entity.Role;
import com.moxi.mogublog.xo.service.AdminRoleService;
import com.moxi.mogublog.xo.service.AdminService;
import com.moxi.mogublog.xo.service.RoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 登录管理RestApi(为了更好地使用security放行把登录管理放在AuthRestApi中)
 * </p>
 *
 * @author limbo
 * @since 2018-10-14
 */
@RestController
@RequestMapping("/auth")
@Api(value="登录管理RestApi",tags={"loginRestApi"})
public class LoginRestApi {
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private JwtHelper jwtHelper;
	
	@Autowired
	private AdminRoleService adminRoleService;
	
	@Autowired
	private Audience audience;
	
	@Value(value="${tokenHead}")
	private String tokenHead;
	
	@Value(value="${isRememberMeExpiresSecond}")
	private int longExpiresSecond;
	
	@ApiOperation(value="用户登录", notes="用户登录")
	@PostMapping("/login")
	public String login(HttpServletRequest request, 
			@ApiParam(name = "username", value = "用户名或邮箱或手机号", required = false) @RequestParam(name = "username", required = false) String username,
			@ApiParam(name = "password", value = "密码", required = false) @RequestParam(name = "password", required = false) String password,
			@ApiParam(name = "isRememberMe", value = "是否记住账号密码", required = false) @RequestParam(name = "isRememberMe", required = false, defaultValue = "0") int isRememberMe){
			
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return ResultUtil.result(SysConf.ERROR, "账号或密码不能为空");
		}		
		  Boolean isEmail = CheckUtils.checkEmail(username);
		  Boolean isMobile = CheckUtils.checkMobileNumber(username);
	      QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
	      if (isEmail) {
	    	  queryWrapper.eq(SQLConf.EMAIL,username);
	      } else if(isMobile){
	    	  queryWrapper.eq(SQLConf.MOBILE,username);
	      }else {
	    	  queryWrapper.eq(SQLConf.USER_NAME,username);
	      }
	      Admin admin = adminService.getOne(queryWrapper);
	      if (admin == null) {
	          return ResultUtil.result(SysConf.ERROR, "管理员账号不存在");
	      }
	      //验证密码
	      PasswordEncoder encoder = new BCryptPasswordEncoder();
	      boolean isPassword = encoder.matches(password, admin.getPassWord());
	      if (!isPassword) {
	          //密码错误，返回提示
	          return ResultUtil.result(SysConf.ERROR, "密码错误");
	      }
	      //根据admin获取账户拥有的角色uid集合
	      QueryWrapper<AdminRole> wrapper = new QueryWrapper<>();
	      wrapper.eq(SQLConf.ADMINUID,admin.getUid());
	      List<AdminRole> adminRoleList = adminRoleService.list(wrapper);
	      List<String> roleUids = new ArrayList<>();
	      for (AdminRole adminRole : adminRoleList) {
			String roleUid = adminRole.getRoleUid();
			roleUids.add(roleUid);
	      }
	      
	      List<Role> roles = (List<Role>) roleService.listByIds(roleUids);
	      String roleNames = null;
	      for (Role role : roles) {
			roleNames+=(role.getRoleName()+",");
		  }
	      String roleName = roleNames.substring(0, roleNames.length()-2);
	      long expiration = isRememberMe==1 ? longExpiresSecond : audience.getExpiresSecond();
	      String jwtToken = jwtHelper.createJWT(admin.getUserName(),
	    		 							   admin.getUid(),
	    		 							   roleName.toString(),
	    		 							   audience.getClientId(),
	    		 							   audience.getName(),
	    		 							   expiration*1000,
	    		 							   audience.getBase64Secret());
		String token = tokenHead + jwtToken;
		Map<String, Object> result = new HashMap<>();
		result.put(SysConf.TOKEN, token);
		return ResultUtil.result(SysConf.SUCCESS, result);
	}
	
	@ApiOperation(value = "用户信息", notes = "用户信息", response = String.class)
	@GetMapping(value = "/info")
	public String info(@ApiParam(name = "token", value = "token令牌",required = false) @RequestParam(name = "token", required = false) String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(SysConf.TOKEN, "admin");
		map.put(SysConf.AVATAR, "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
		List<String> list = new ArrayList<String>();
		list.add("Administrator");
		map.put("roles", list);		
		return ResultUtil.result(SysConf.SUCCESS, map);
	}
	
//	@ApiOperation(value="用户登录", notes="用户登录")
//	@PostMapping("/login")
//	public String login(HttpServletRequest request, 
//			@ApiParam(name = "username", value = "用户名或邮箱或手机号", required = true) @RequestParam(name = "username", required = true) String username,
//			@ApiParam(name = "password", value = "密码", required = true) @RequestParam(name = "password", required = true) String password){
//		
//		UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password); 	
//		final Authentication authentication = authenticationManager.authenticate(upToken);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        
//        // Reload password post-security so we can generate token
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//        final String token = jwtHelper.createJWT(userDetails.getUsername(),
//												 userDetails.getAuthorities(),
//												 audience.getClientId(),
//												 audience.getName(),
//												 audience.getExpiresSecond()*1000,
//												 audience.getBase64Secret());
//        return token;
//	}
	
	@ApiOperation(value = "退出登录", notes = "退出登录", response = String.class)
	@PostMapping(value = "/logout")
	public String logout(@ApiParam(name = "token", value = "token令牌",required = false) @RequestParam(name = "token", required = false) String token) {	
		String destroyToken = null;
		return ResultUtil.result(SysConf.SUCCESS, destroyToken);
	}
	
}