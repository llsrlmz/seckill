package com.lmz.seckill.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lmz.seckill.dto.Exposer;
import com.lmz.seckill.dto.SeckillExecution;
import com.lmz.seckill.entity.Seckill;
import com.lmz.seckill.exception.RepeatKillException;
import com.lmz.seckill.exception.SeckillCloseException;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉 junit spring配置文件
@ContextConfiguration({
	"classpath:spring/spring-dao.xml",
	"classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	@Test
	public void testGetSeckillList() throws Exception {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list);
	}
	
	
	@Test
	public void testGetById() throws Exception {
		long id = 1000L;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}", seckill);
	}
	
	// 集成测试代码完整逻辑，注意可重复执行
	@Test
	public void testSeckillLogic() throws Exception {
		long id = 10019L;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if(exposer.isExposed()) {
			logger.info("exposer={}", exposer);
			long phone = 13332325315L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
				logger.info("seckillExecution={}", seckillExecution);
			} catch(RepeatKillException e) {
				logger.error(e.getMessage());
			} catch(SeckillCloseException e) {
				logger.error(e.getMessage());
			} 
		} else {
			// 秒杀未开启
			logger.warn("exposer={}", exposer);
		}
		
	}
	
	@Test
	public void executeSeckillProcedure() throws Exception {
		long seckillId = 1001L;
		long phone = 13684736543L;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if(exposer.isExposed()) {
			String md5 = exposer.getMd5();
			SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
			logger.info(execution.getStateInfo());
		}
	}
	
/*	
	@Test
	public void testExportSeckillUrl() throws Exception {
		long id = 1000L;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}", exposer);
		// exposer=Exposer [exposed=true, md5=431b1ec98b50c9074544e326ba9a4c6c, seckillId=1000, now=0, start=0, end=0]
	}
	
	@Test
	public void testExecuteSeckill() throws Exception {
		long id = 1000L;
		long phone = 13332325315L;
		String md5 = "431b1ec98b50c9074544e326ba9a4c6c";
		try {
			SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
			logger.info("seckillExecution={}", seckillExecution);
		} catch(RepeatKillException e) {
			logger.error(e.getMessage());
		} catch(SeckillCloseException e) {
			logger.error(e.getMessage());
		} 
	}
*/

}
