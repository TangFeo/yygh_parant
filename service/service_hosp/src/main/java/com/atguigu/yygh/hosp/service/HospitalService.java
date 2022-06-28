package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.data.domain.Page;
import java.util.Map;


public interface HospitalService {
    void save(Map<String, Object> paramMap);
    Hospital getByHoscode(String hoscode);
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String, Object> getHospByid(String id);
    //获取医院名称
    String getHospName(String hoscode);

    /**
     * 根据医院编号获取医院名称接口
     * @param hoscode
     * @return
     */
    String getName(String hoscode);

}
