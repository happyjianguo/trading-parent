package com.dili.trading.rpc;

import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.assets.sdk.dto.CarTypePublicDTO;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "assets-service", contextId = "assets")
public interface AssetsRpc {
    /**
     * 获取车型
     */
    @RequestMapping(value = "/api/carType/listCarType", method = RequestMethod.POST)
    BaseOutput<List<CarTypeDTO>> listCarType(CarTypePublicDTO carTypePublicDTO);

    @RequestMapping(value = "/api/customCategory/getTree", method = RequestMethod.POST)
    BaseOutput<List<CategoryDTO>> list(CategoryDTO categoryDTO);

}
