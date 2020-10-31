package com.lemoncc.service.role;

import com.lemoncc.dao.BaseDao;
import com.lemoncc.dao.role.RoleDao;
import com.lemoncc.dao.role.RoleDaoImpl;
import com.lemoncc.pojo.Role;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RoleServiceImpl implements RoleService{
    private RoleDao roleDao;
    public RoleServiceImpl() {
        roleDao = new RoleDaoImpl();
    }

    // 获取角色列表
    @Override
    public List<Role> getRoleList() {
        Connection connection = null;
        List<Role> list = null;
        try {
            connection = BaseDao.getConnection();
            list = roleDao.getRoleList(connection);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            BaseDao.closeResource(connection, null, null);
        }

        return list;
    }

    @Test
    public void test(){
        List<Role> list = getRoleList();
        for (Role role : list) {
            System.out.println(role.getId());
            System.out.println(role.getRoleName());
            System.out.println(role.getRoleCode());
            System.out.println("----------------");
        }
    }
}
