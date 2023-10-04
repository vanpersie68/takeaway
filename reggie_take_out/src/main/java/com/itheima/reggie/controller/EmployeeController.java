package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController
{
    @Autowired
    private EmployeeService employeeService;

    //员工登录
    @PostMapping("/login")
    /*@RequestBody主要用来接收前端传递给后端的json字符串中的数据(请求体中的数据的)；GET方式无请求体，
    所以使用@RequestBody接收数据时，前端不能使用GET方式提交数据，而是用POST方式进行提交。*/
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee)
    {
        //1.将页面提交的密码password 进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //对密码进行加密处理

        //2. 根据页面提交的用户名username查询数据库
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", employee.getUsername()); //作为查询条件
        Employee emp = employeeService.getOne(queryWrapper); //相当于selectOne的作用

        //3. 如果没有查询到返回登录失败结果
        if(emp == null)
            return R.error("登录失败");

        //4. 密码对比，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password))
            return R.error("登陆失败");

        //5. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0) //0表示员工禁用，1表示可用
            return R.error("账号已禁用");

        //6. 登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    //员工退出系统
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request)
    {
        //清理session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //添加员工
    @PostMapping
    public R<String> save(@RequestBody Employee employee)
    {
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //save方法是mybaits-plus封装好的方法，相当于insert操作
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    //员工信息分页查询: 将员工信息通过分页的方式显示到后台上
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //构造条件构造器
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();

        //添加查询条件 (如果name不为空，才会添加这个条件)
        queryWrapper.like(!StringUtils.isEmpty(name),"name",name);

        //添加排序条件
        queryWrapper.orderByDesc("update_time");

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    //根据id修改员工的信息，如（status）
    @PutMapping //这里参数中的employee对象是 被修改的员工
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee)
    {
        log.info(employee.toString());

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable String id)
    {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null)
        {
            return R.success(employee);
        }

        return R.error("没有查询到对应员工信息");
    }
}
