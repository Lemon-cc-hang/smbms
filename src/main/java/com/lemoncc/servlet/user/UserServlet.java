package com.lemoncc.servlet.user;

import com.alibaba.fastjson.JSONArray;
import com.lemoncc.pojo.Role;
import com.lemoncc.pojo.User;
import com.lemoncc.service.role.RoleService;
import com.lemoncc.service.role.RoleServiceImpl;
import com.lemoncc.service.user.UserService;
import com.lemoncc.service.user.UserServiceImpl;
import com.lemoncc.util.Constants;
import com.lemoncc.util.PageSupport;
import com.mysql.jdbc.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 实现Servlet复用
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if (method != null) {
            switch (method) {
                case "savepwd":
                    this.updatePwd(req, resp);
                    break;
                case "query":
                    this.query(req, resp);
                    break;
                case "ucexist":
                    this.isUserCodeExist(req, resp);
                    break;
                case "getrolelist":
                    this.getRoleList(req, resp);
                    break;
                case "add":
                    this.addUser(req, resp);
                    break;
                case "pwdmodify":
                    this.pwdModify(req, resp);
                    break;
                case "deluser":
                    this.delUser(req, resp);
                    break;
                case "modify":
                    this.getUserById(req, resp, "/jsp/usermodify.jsp");
                    break;
                case "view":
                    this.getUserById(req, resp, "/jsp/userview.jsp");
                    break;
                case "modifyexe":
                    this.modify(req, resp);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    // 重点, 难点
    private void query(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 查询用户列表
        // 从前端获取数据
        String queryUserName = req.getParameter("queryname");
        String userRole = req.getParameter("queryUserRole");
        String pageIndex = req.getParameter("pageIndex");
        int queryUserRole = 0;

        // 获取用户列表
        List<User> userList = null;
        UserService userService = new UserServiceImpl();

        // 第一次走这个请求, 一定是第一页, 页面大小是固定的
        int currentPageNo = 1;
        if (queryUserName == null) {
            queryUserName = "";
        }
        if (!StringUtils.isNullOrEmpty(userRole)) {
            queryUserRole = Integer.parseInt(userRole);
        }
        if (pageIndex != null) {
            currentPageNo = Integer.parseInt(pageIndex);
        }
        // 获取用户的总数
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);

        // 总页数支持
        PageSupport pageSupport = new PageSupport();
        pageSupport.setCurrentPageNo(currentPageNo);
        pageSupport.setPageSize(Constants.pageSize);
        pageSupport.setTotalCount(totalCount);

        int totalPageCount = pageSupport.getTotalPageCount();

        // 控制首页和尾页
        // 如果页面要小于1, 就显示第一页的东西
        if (currentPageNo < 1) {
            currentPageNo = 1;
        } else if (currentPageNo > totalPageCount) {
            currentPageNo = totalPageCount;
        }

        // 获取用户列表
        userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, Constants.pageSize);

        // 获取角色列表
        RoleService roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList();

        // 存入页面
        req.setAttribute("roleList", roleList);
        req.setAttribute("userList", userList);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("currentPageNo", currentPageNo);
        req.setAttribute("totalPageCount", totalPageCount);

        // 返回前端
        try {
            req.getRequestDispatcher("/jsp/userlist.jsp").forward(req, resp);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    // 更新密码
    public void updatePwd(HttpServletRequest req, HttpServletResponse resp) {
        // 从Session拿id
        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        String newpassword = req.getParameter("newpassword");
        boolean flag = false;
        if (o != null && !StringUtils.isNullOrEmpty(newpassword)) {
            UserService userService = new UserServiceImpl();
            flag = userService.updatePwd(((User) o).getId(), newpassword);
            if (flag) {
                req.setAttribute(Constants.SYS_MESSAGE, "修改密码成功, 请退出, 使用新密码登录");
                // 密码修改成功, 移除当前Session
                req.getSession().removeAttribute(Constants.USER_SESSION);
            } else {
                req.setAttribute(Constants.SYS_MESSAGE, "修改密码失败");

            }
        } else {
            req.setAttribute(Constants.SYS_MESSAGE, "修改密码失败");
        }
        try {
            req.getRequestDispatcher("/jsp/pwdmodify.jsp").forward(req, resp);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    // 验证旧密码
    public void pwdModify(HttpServletRequest req, HttpServletResponse resp) {
        // 获取输入的旧密码
        String oldpassword = req.getParameter("oldpassword");
        // 获取登录的密码
        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        // 万能的Map集
        Map<String, String> resultMap = new HashMap<String, String>();

        if (o == null) {
            resultMap.put("result", "sessionerror");
        } else if (StringUtils.isNullOrEmpty(oldpassword)) {
            resultMap.put("result", "error");
        } else {
            if (((User) o).getUserPassword().equals(oldpassword)) {
                resultMap.put("result", "true");
            } else {
                resultMap.put("result", "false");
            }
        }
        try {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            // JSONArray 阿里巴巴的JSON工具类, 转换格式
            writer.write(JSONArray.toJSONString(resultMap));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 验证userCode是否存在
    public void isUserCodeExist(HttpServletRequest req, HttpServletResponse resp) {
        String userCode = req.getParameter("userCode");
        Map<String, String> resultMap = new HashMap<String, String>();
        if (StringUtils.isNullOrEmpty(userCode)) {
            resultMap.put("userCode", "exist");
        } else {
            UserService userService = new UserServiceImpl();
            boolean flag = userService.isUserCodeExist(userCode);
            if (flag) {
                resultMap.put("userCode", "exist");
            } else {
                resultMap.put("userCode", "notexist");
            }
        }
        try {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            // JSONArray 阿里巴巴的JSON工具类, 转换格式
            writer.write(JSONArray.toJSONString(resultMap));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取角色列表
    public void getRoleList(HttpServletRequest req, HttpServletResponse resp) {
        RoleService roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList();
        try {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            // JSONArray 阿里巴巴的JSON工具类, 转换格式
            writer.write(JSONArray.toJSONString(roleList));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 增加用户
    public void addUser(HttpServletRequest req, HttpServletResponse resp) {
        // 获取角色信息
        String userCode = req.getParameter("userCode");
        String userName = req.getParameter("userName");
        String userPassword = req.getParameter("userPassword");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");

        // 设置User信息
        User user = new User();
        user.setUserCode(userCode);
        user.setUserName(userName);
        user.setUserPassword(userPassword);
        user.setGender(Integer.valueOf(gender));
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));
        user.setCreationDate(new Date());
        user.setCreatedBy(((User) req.getSession().getAttribute(Constants.USER_SESSION)).getId());

        // 添加
        UserService userService = new UserServiceImpl();
        try {
            if (userService.add(user)) {
                resp.sendRedirect(req.getContextPath() + "/jsp/user.do?method=query");
            } else {

                req.getRequestDispatcher("/jsp/useradd.jsp").forward(req, resp);
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    // 删除用户
    public void delUser(HttpServletRequest req, HttpServletResponse resp) {
        // 获取要删除的id
        String id = req.getParameter("uid");
        Integer delId = 0;
        try {
            delId = Integer.parseInt(id);
        } catch (Exception e) {
            delId = 0;
        }

        // 删除的结果
        Map<String, String> resultMap = new HashMap<>();
        if (delId <= 0) {
            resultMap.put("delResult", "notexist");
        } else {
            UserService userService = new UserServiceImpl();
            if (userService.deleteUserById(delId)) {
                resultMap.put("delResult", "true");
            } else {
                resultMap.put("delResult", "false");
            }
        }

        // 将resultMap转换为JSON对象
        try {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            writer.write(JSONArray.toJSONString(resultMap));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 查看和修改用户
    public void getUserById(HttpServletRequest req, HttpServletResponse resp, String url) {
        String id = req.getParameter("uid");
        Integer getId;
        try {
            getId = Integer.parseInt(id);
        } catch (Exception e) {
            getId = 0;
        }
        if (!StringUtils.isNullOrEmpty(id)) {
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(getId);
            req.setAttribute("user", user);
            try {
                req.getRequestDispatcher(url).forward(req, resp);
            } catch (ServletException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 修改用户信息
    public void modify(HttpServletRequest req, HttpServletResponse resp) {
        // 获取更改后的用户信息
        String id = req.getParameter("uid");
        String userName = req.getParameter("userName");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");

        // 更改用户信息, 存入user
        User user = new User();
        user.setId(Integer.parseInt(id));
        user.setUserName(userName);
        user.setGender(Integer.valueOf(gender));
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));
        user.setModifyDate(new Date());
        user.setModifyBy(((User) req.getSession().getAttribute(Constants.USER_SESSION)).getId());

        // 修改
        UserService userService = new UserServiceImpl();
        try {
            if (userService.modify(user)) {
                resp.sendRedirect(req.getContextPath() + "/jsp/user.do?method=query");
            } else {
                req.getRequestDispatcher("/jsp/usermodify.jsp").forward(req, resp);
            }
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
    }
}
