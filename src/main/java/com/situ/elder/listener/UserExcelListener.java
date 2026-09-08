package com.situ.elder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.pojo.vo.UserExcelVO;
import com.situ.elder.util.PasswordUtil;
import org.springframework.beans.BeanUtils;

public class UserExcelListener extends AnalysisEventListener<UserExcelVO> {

    private UserMapper userMapper;

    public UserExcelListener(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void invoke(UserExcelVO userExcelVO, AnalysisContext analysisContext) {
        User user = new User();
        BeanUtils.copyProperties(userExcelVO, user);
        user.setId(null);
        //导入用户密码加密：无密码则默认 123456
        String raw = (user.getPassword() == null || user.getPassword().isEmpty()) ? "123456" : user.getPassword();
        user.setPassword(PasswordUtil.encode(raw));
        userMapper.insert(user);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
