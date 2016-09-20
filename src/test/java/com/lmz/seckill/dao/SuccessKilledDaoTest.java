package com.lmz.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lmz.seckill.entity.SuccessKilled;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉 junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

	//注入Dao 实现类依赖
	@Resource
	private SuccessKilledDao successSeckilledDao;
	
	@Test
	public void testInsertSuccessKilled() throws Exception {
		int insertCount = successSeckilledDao.insertSuccessKilled(1001L, 13261828683L);
		System.out.println("insertCount: " + insertCount);
	}
	
	@Test
	public void testQueryByIdWithSeckill() throws Exception {
		SuccessKilled successKilled = successSeckilledDao.queryByIdWithSeckill(1001L, 13261828683L);
		System.out.println(successKilled);
		System.out.println(successKilled.getSeckill());
	}

}
