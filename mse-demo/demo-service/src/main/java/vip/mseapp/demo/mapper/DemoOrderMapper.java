package vip.mseapp.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import vip.mseapp.demo.entity.DemoOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoOrderMapper extends BaseMapper<DemoOrder> {
}
