package vip.mseapp.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import vip.mseapp.demo.entity.DemoEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoEventMapper extends BaseMapper<DemoEvent> {
}
