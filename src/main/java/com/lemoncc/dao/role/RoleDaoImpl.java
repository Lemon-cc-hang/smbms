package com.lemoncc.dao.role;

import com.lemoncc.dao.BaseDao;
import com.lemoncc.pojo.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao{
    // 获取角色列表
    @Override
    public List<Role> getRoleList(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Role> list = new ArrayList<Role>();
        if (connection != null) {
            String sql = "select * from smbms_role;";
            Object[] params = {};
            ResultSet resultSet = BaseDao.execute(connection, preparedStatement, rs, sql, params);
            while (resultSet.next()) {
                Role _role = new Role();
                _role.setId(resultSet.getInt("id"));
                _role.setRoleCode(resultSet.getString("roleCode"));
                _role.setRoleName(resultSet.getString("roleName"));
                _role.setCreatedBy(resultSet.getInt("createdBy"));
                _role.setCreationDate(resultSet.getDate("creationDate"));
                _role.setModifyBy(resultSet.getInt("modifyBy"));
                _role.setModifyDate(resultSet.getDate("modifyDate"));
                list.add(_role);
            }
            BaseDao.closeResource(null, preparedStatement, resultSet);
        }
        return list;
    }
}
