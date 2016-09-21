**本项目是基于慕课网课程-Java高并发秒杀API，完整学习并自己手敲全部代码，下面是项目业务介绍和所用技术及学习总结：**
#一.业务介绍
首先是4钟秒杀商品，如下图：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/1.png)

点击link可以跳转到当前商品的详情页，首先需要验证当前用户，由于用户模块不是本项目重点，所以只简单通过cookie验证了手机号，如果跳转中发现cookie中没有手机号，则提示先输入手机号，如下图：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/2.png)

点击Submit，会对手机号验证，输入手机号格式不正确会有验证提示：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/3.png)

输入正确后将手机号写入cookie中，并给出当前商品什么时候可以开始秒杀：  
秒杀还未开始：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/4.png)

秒杀结束：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/5.png)

如果可以开始秒杀，Ajax会先发送请求获取采用了md5加密过的秒杀地址接口然后用户点击“开始秒杀”会向这个地址发送秒杀请求进行秒杀：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/6.png)

秒杀成功后会提示：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/7.png)

同一个商品一个手机号只能秒杀成功一次，否则提示重复秒杀：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/8.png)

如商品被抢购一空，没抢到会提示：
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/9.png)

-------


#二.所用技术及简单总结：
项目采用：SpringMVC,Spring,MyBatis的MVC构架，数据库采用MySQL，并由于抢购商品会被大量访问，将商品数据存放到了Redis中，前端采用Bootstrap，jQuery及几个插件(倒计时、弹窗等)，并且对JavaScript代码进行了模块化编写和管理。业务层和控制层均对传输数据进行了一层DTO封装，及对异常信息的封装整理成运行期异常抛给控制层，这样spring对业务层的事务控制可以正确Commit或Rollback因为Spring对业务层方法抛出的非运行期异常无法正确控制事务，并采用基于注解的方式对事务进行控制，这样的好处是：1).开发团队达成一致约定，明确标注事物方法的编程风格。2).保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事物方法外部。3).不是所有的方法方法都需要事务，如只有一条修改操作，只读操作是不需要事务控制的。使用枚举表述常量数据字段。采用了RESTful风格的接口。并使用JUnit4编写测试用例。

-------

#三.分析秒杀的瓶颈并对其进行优化：
###前端方面：  
1.首先采用一些编程上的技巧，比如秒杀按钮的点击事件只绑定一次Ajax请求(jQuery的one函数)，由于一个手机号对一件商品只能成功抢一次，这样首先可以过滤掉一部分无效请求。  

2.刚进入详情页的时候，会首先用Ajax发送一次请求获取系统时间，这是因为当用户获取具体商品的秒杀详情页(detail.html)的时候，很可能会不断刷新，就会有大量刷新详情页，此时我们把详情页和一些静态资源部署到cdn节点上，如图，这时我们就需要做一个请求来获取服务器的时间。  
	
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/10.png)

**对cdn的理解：**    
	![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/11.png)  
	(1).这个获取资源可以是静态资源也可以是动态资源，取决于我们的获取策略。如大部分视频资源加速都是部署在cdn上。  
	(2).获取系统时间是不用优化的，因为：java在访问一次内存大概需要10ns，1秒=10亿纳秒，我们访问系统时间本质上也就是new了一个日期对象，返回给用户，如果不考虑GC的影响的话，它1秒钟可以做1亿次，所以这个操作完全不用优化，因为它没有任何的后端访问。  
	3.对于前端的优化还有很多技巧很多比如合理正确地使用Http的Cache-control/Expires等标头，对引入的放在了cdn上的css、js等采用“数据摘要算法”以便更新部分文件时不用全部更新如:<link href=http://xxx/a.css?v=uwefihs8732fa>，可以更好的利用用户浏览器缓存更极致的节省流量等等等等。  
**总结可归纳4点：** 

	(1).配置超长时间的本地缓存 —> 节省带宽，提高性能  
	
	(2).采用内容摘要作为缓存更新依据 —> 精确的缓存控制  
	
	(3).静态资源CDN部署 —> 优化网络请求  
	
	(4).更新资源发布路径实现非覆盖式发布 —> 平滑升级  
	


###后端： 

cdn放静态资源不变化的，后端缓存可以用redis(每秒钟10万qps，做成集群更是可以上百万的qps)之类的，把数据库常用资源缓存在redis中。  


![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/12.png)
  
下面这个方案：可以先采用redis或其他NoSQL做一个原子计数器，存储秒杀个数和相应的用户信息(如账号、电话等)。然后采用分布式的消息队列对数据进行处理这是一个典型的生产者消费者场景。最后从MQ中取出消费行为作出相应处理后记录到MySQL中。见下图：


![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/13.png)


对于这个构架：

	好处：这个架构可以抗住非常高的并发。  
	痛点：  
		(1).运维成本和稳定性：NoSQL,MQ等：这些都是分布式服务，就像我们的MySQL一样都是部署在不同的。NoSQL远远没有MySQL稳定，所以它需要一个非常强大的运维团队来去支撑这些组件的稳定性。  
	    (2).开发成本：数据一致性回滚方案等：需要工程师要对这些组件非常的熟悉，熟悉它们的数据一致性模型，以及自己的逻辑应该怎么处理回滚，如:比如当我们的减库存失败了，或者当我们减了库存然后发现MQ访问超时了怎么回滚，这些都需要手动操作。  
	    (3).幂等性难保证：重复秒杀问题：就是当它减库存的时候，还不知道当前这个用户还有没有减过库存，这个时候它就还会发一个消息(MQ)，然后告诉它这个用户又去对这个商品做了秒杀，那么这个时候还会去维护一个NoSQL的IO(访问)方案，这个同时加大了成本。		
	    (4).不适合新手的架构  
**总结为下图：**  
  	![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/14.png)
  	

###为什么不用MySQL解决？：  
	认为MySQL低效。  
	
	做个测试：用一个id执行update减库存。-> 每秒4万qps，也就是同一个商品每秒可以被卖4万次。  
	
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/15.png)  
  
###Java控制事物行为分析：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/16.png)  

可见后面的用户都在等，那么这是一个串行化的操作，会有大量的阻塞操作，比如这一完整流程执行下来用时2毫秒，也就是1秒钟只能有500  次的减库存的秒杀操作，500次对于大部分系统来说是ok的，但对于秒杀系统特别是热点的系统来说其实是不能满足我们的要求的，特别是当  排队非常长的时候，性能会呈指数级下降。
  
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/17.png)  

经过分析，可知优化方向是减少行级锁持有时间：  

对延迟的分析：  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/18.png)  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/19.png)  

###如何判断数据库update成功：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/20.png)  

  如何放到MySQL端：修改MySQL源码，事务的commit或rollback都放在MySQL中不让Java端程序控制，这种方案如：阿里巴巴天猫，但成本较高一般公司不建议。第二种就是使用存储过程。  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/21.png)

##总结：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/22.png)  

对于商品时候可以开始秒杀的查询，也就是秒杀地址接口的暴露，我们可以用redis来优化：  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/23.png)  

由于redis并没有实现内部序列化操作，那就就需要我们在向redis存取前自己先对数据序列化，既然要对高并发优化就做到极致，jdk自己的  实现Serializable接口并不是最优的，本项目采用一个开源社区方案：protostuff，它只能序列化有get()set()方法的对象，而不能序列化String等。

###对于并发的优化：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/24.png)  

(1).优化方案1 —> 简单优化：  
		
通过上图对事务的分析可知：在行级锁期间会有两次网络延迟和GC，如果我们把两条sql调换操作执行的顺序，由我们用的是insert ignore可根据返回是1或0来得知之后重复秒杀了，此时可据此来挡住一部分重复秒杀操作，锁只在第二条减库存的sql上了，可见在锁期间减少了1倍网络延迟和GC，最终达到降低MySQL的rowLock的持有时间。网络延迟或客户端延迟(GC)对MySQL行级锁高并发竞争的事务来说的话是一个性能杀手，我们的目标就是降低行级锁到commit这个过程的持续时间，同时来让MySQL来获得更高的QPS。如下图所示：  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/25.png)  

(2).优化方案2，深度优化 —> 事务SQL在MySQL端执行（存储过程）  
	

##系统部署构架：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/26.png)  

-首先通过把静态资源放到CDN上，这些请求就不会再访问我们的服务器，来达到降低我们服务器的访问量度。  
-第二WebServer，我们一般不直接把WebServer直接对外访问，一般前面会放Nginx，Nginx会是一个集群化的，部署在多台服务器上做ACP服务  器，并且帮后端的Jetty或Tomcat这样的服务器做反向代理，负载均衡。  
-再有用Redis来做服务器端的缓存，达到热点数据的快速存取的目的。  
-最后用MySQL的事务来达到秒杀的数据的一致性和完整性。  


##大型系统的架构如下图：

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/27.png)  

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/28.png)  
  
#最后总结：

**1.数据层技术回顾：**  

1).数据库就是表的设计和熟练手写DDL  
2).DAO层每个方法实现一个sql，在Service层来组合调用来完成业务逻辑。  
3).MyBatis和Spring的高效使用，通过配置一下包扫描和别名的使用等技巧来达到一次配置之后不需要修改。  
		

![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/29.png)  
  	
**2.业务层技术回顾：**  
1).站在使用者的角度去设计接口，而不是想着这个接口如何去实现  
2).SpringIOC使用xml配置注解的方式  
3).掌握事务什么情况回滚和提交  
		
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/30.png)  

**3.WEB技术回顾：**  
1).Restful接口是来描述一个资源，通过不同的提交方式来达到描述行为的目的  
2).SpringMVC使用技巧，如何配置，参数的映射，以及如何把数据打包成json返回给浏览器  
3).前端交互很重要不要忽略，尤其是一些复杂产品的需求  
4).Bootstrap和JS的使用，JS的模块化以及以下良好的面向对象的机制去书写json 
		
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/31.png)  
  	
**4.并发优化:**  

1).分析找到性能瓶颈来优化  
2).有事务必然有锁，对于长时间持有锁的程序我们需要考虑网络延迟，对于Java程序也会有GC的影响，对于这些如何去避免。  
3).对于各层面上缓存的理解和使用  
4).进行集群化部署    
		
![image](https://github.com/llsrlmz/seckill/raw/master/src/main/webapp/resources/imags/readme/32.png)  
  	
谢谢您阅读，如有有误之处，请您斧正。3q ^.^ .  
	
如下载本项目并运行除了别忘了修改成您自己的配置文件和建立两个数据库，记得把项目在webserver中path修改为“/”。  :)
