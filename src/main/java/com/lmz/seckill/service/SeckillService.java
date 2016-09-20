package com.lmz.seckill.service;

import java.util.List;

import com.lmz.seckill.dto.Exposer;
import com.lmz.seckill.dto.SeckillExecution;
import com.lmz.seckill.entity.Seckill;
import com.lmz.seckill.exception.RepeatKillException;
import com.lmz.seckill.exception.SeckillCloseException;
import com.lmz.seckill.exception.SeckillException;

/**
 * 业务接口: 站在"使用者"的角度设计接口
 * 	三个方面：方法定义粒度，参数，返回类型(return 类型/异常)
 * @author LMZ
 *
 */
public interface SeckillService {

	/**
	 * 查询所有秒杀记录
	 * @return
	 */
	List<Seckill> getSeckillList();
	
	/**
	 * 查询单个秒杀记录
	 * @param seckillId
	 * @return
	 */
	Seckill getById(long seckillId);
	
	/**
	 * 秒杀开启时，输出秒杀接口地址，否则输出系统时间和秒杀时间
	 * @param seckillId
	 */
	Exposer exportSeckillUrl(long seckillId);
	
	/**
	 * 执行秒杀操作
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 */
	SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
		throws SeckillException, RepeatKillException, SeckillCloseException;
	
	/**
	 * 执行秒杀操作 by 存储过程
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 */
	SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)
		throws SeckillException, RepeatKillException, SeckillCloseException;
	
}







