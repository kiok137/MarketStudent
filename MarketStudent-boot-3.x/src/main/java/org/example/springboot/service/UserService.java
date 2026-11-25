package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Menu;
import org.example.springboot.entity.Role;
import org.example.springboot.entity.User;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.RoleMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private MenuService menuService;

    @Resource
    private RoleMapper roleMapper;
    
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    public User getByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        loadUserRole(user);
        return user;
    }

    public User login(User user) {
        User dbUser = getByUsername(user.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理
        if (dbUser.getStatus().equals(AccountStatus.DISABLED.getValue())) {
            throw new ServiceException("账号已被禁用");
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        
        // 获取用户的菜单权限
        List<Menu> menus = menuService.getMenusByRoleId(dbUser.getRoleId());
        String token = JwtTokenUtils.genToken(String.valueOf(dbUser.getUserId()), dbUser.getPassword());
        
        dbUser.setMenuList(menus);
        dbUser.setToken(token);
        return dbUser;
    }

    public List<User> getUserByRole(Integer roleId) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleId, roleId)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        users.forEach(this::loadUserRole);
        return users;
    }

    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        if(user.getRoleId() == null){
            user.setRoleId(2);
        }
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // 设置默认密码
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    public void updateUser(Integer userId, User user) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new ServiceException("要更新的用户不存在");
        }
        
        // 检查新用户名是否与其他用户重复
        if (user.getUsername() != null) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            );
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                throw new ServiceException("新用户名已被使用");
            }
        }
        
        // 设置更新时间
        user.setUpdatedAt(LocalDateTime.now());
        user.setUserId(userId);
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("用户更新失败");
        }
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        loadUserRole(user);
        return user;
    }

    public void deleteBatch(List<Integer> ids) {
        if (userMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        if (users.isEmpty()) {
            throw new ServiceException("未找到任何用户");
        }
        users.forEach(this::loadUserRole);
        return users;
    }

    public User getUserById(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        loadUserRole(user);
        return user;
    }

    public Page<User> getUsersByPage(String username, String realName, Integer roleId, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(realName)) {
            queryWrapper.like(User::getRealName, realName);
        }
        if (roleId != null) {
            queryWrapper.eq(User::getRoleId, roleId);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        Page<User> page = userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        // 加载每个用户的角色信息
        page.getRecords().forEach(this::loadUserRole);
        return page;
    }

    public void updatePassword(Integer userId, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败");
        }
    }

    public void deleteUserById(Integer userId) {
        if (userMapper.deleteById(userId) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    // 加载用户的角色信息
    private void loadUserRole(User user) {
        if (user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            user.setRole(role);
        }
    }
}
