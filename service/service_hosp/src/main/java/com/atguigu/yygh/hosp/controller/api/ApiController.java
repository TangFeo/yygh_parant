package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
/**
 * 这是医院系统调用的接口
 **/


@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request){
        //得到医院传过来的数据
        Map<String, String[]> requestMap = request.getParameterMap();
        //将数组装换成Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //获取医院系统传过来的签名,签名进行了MD5加密
        String hospSign = (String) paramMap.get("sign");
        System.out.println("医院系统中的签名："+hospSign);

        //图片转换为base64字符串时，该字符串中包含大量的加号“+”，服务器在解析数据时会把加号当成连接符，转换为空格，
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replace(" ","+");
        paramMap.put("logoData",logoData);

        //得到hospital_set中的签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey =  hospitalSetService.getSignKey(hoscode);
        //把hospital_set中查出来的签名进行MD5加密
        String signKeyMD5 = MD5.encrypt(signKey);
        System.out.println("医院管理中的签名："+signKeyMD5);

        if(!signKeyMD5.equals(hospSign)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            hospitalService.save(paramMap);
            return Result.ok();
        }
    }

    //查询医院
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        //得到医院传过来的数据
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        String hoscode = (String) paramMap.get("hoscode");
        String hospSign = (String) paramMap.get("sign");
        String signKey = hospitalSetService.getSignKey(hoscode);

        String signKeyMD5 = MD5.encrypt(signKey);

        if (!signKeyMD5.equals(hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            Hospital hospital = hospitalService.getByHoscode(hoscode);
            return Result.ok(hospital);
        }
    }

    //上传科室接口
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hoscode = (String) paramMap.get("hoscode");
        String hospSign = (String) paramMap.get("sign");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        if(!signKeyMD5.equals(hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            departmentService.save(paramMap);
        }
        return Result.ok();
    }

    //查询科室
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //获取page limit;
        int page;
        int limit;
        if(paramMap.get("page") == null) {
            page = 1;
        }else {
            page = Integer.parseInt((String) paramMap.get("page"));
        }
        if(paramMap.get("limit") == null) {
            limit = 1;
        }else {
            limit = Integer.parseInt((String) paramMap.get("limit"));
        }
        //TODO 签名校验
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> pageModel = departmentService.findPageDepartment(page,limit,departmentQueryVo);
        return Result.ok(pageModel);
    }

    //删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //得到医院编号和科室编号
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        //TODO 签名校验
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    //上传排班信息
    @PostMapping("saveSchedule")
    public Result savaSchedule(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hoscode = (String)paramMap.get("hoscode");
        //签名校验
//        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
//            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
//        }
        scheduleService.save(paramMap);
        return Result.ok();
    }

    //查询排班信息
    @PostMapping("schedule/list")
    public Result findPageSchedule(HttpServletRequest request){
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)paramMap.get("hoscode");
        //非必填
        String depcode = (String)paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String)paramMap.get("limit"));
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.findPageSchedule(page , limit, scheduleQueryVo);
        return Result.ok(pageModel);
    }

    //删除排班信息
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)paramMap.get("hoscode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");
        //签名校验
//        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
//            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
//        }
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }
}
