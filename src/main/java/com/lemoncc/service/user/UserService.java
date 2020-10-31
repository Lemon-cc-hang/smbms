package com.lemoncc.service.user;

import com.lemoncc.pojo.User;

import java.util.List;

public interface UserService {
    // 用户登录
    public User login(String userCode, String password);

    // 根据用户ID修改密码
    public boolean updatePwd(int id, String password);

    // 查询记录数
    public int getUserCount(String userName, int userRole);

    // 根据条件查询用户列表
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize);

    // 验证userCode是否存在
    public boolean isUserCodeExist(String userCode);

    // 增加用户
    public boolean add(User user);

    // 删除用户
    public boolean deleteUserById(int id);

    // 查找用户
    public User getUserById(int id);

    // 修改用户
    public boolean modify(User user);
}
