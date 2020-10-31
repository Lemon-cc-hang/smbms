package com.lemoncc.dao.user;

import com.lemoncc.pojo.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    // 得到要登录的用户
    public User getLoginUser(Connection connection, String userCode) throws SQLException;

    // 修改当前用户密码
    public int updatePwd(Connection connection, int id, String password)throws SQLException;

    // 根据用户名或者用户角色查询用户总数
    public int getUserCount(Connection connection, String userName, int userRole)throws SQLException;

    // 通过条件查询-userList
    public List<User> getUserList(Connection connection, String userName, int userRole, int currentPageNo, int pageSize) throws SQLException;

    // 增加用户
    public int add(Connection connection, User user)throws SQLException;

    // 删除用户
    public int deleteUserById(Connection connection, int id) throws SQLException;

    // 查找用户
    public User getUserById(Connection connection, int id) throws SQLException;

    // 修改用户
    public int modify(Connection connection, User user) throws SQLException;
}
