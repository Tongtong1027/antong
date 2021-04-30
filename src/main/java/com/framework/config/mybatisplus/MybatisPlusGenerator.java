package com.framework.config.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.BeetlTemplateEngine;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: chenkaihang
 * date: 2020/8/11 4:04 下午
 */
public class MybatisPlusGenerator {

    public static void main(String[] args) {
        // 需要构建一个 代码自动生成器 对象
        AutoGenerator mpg = new AutoGenerator();

        // 配置策略
        // 1、全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = "/Users/chenkaihang/IdeaProjects/antong";
        gc.setOutputDir(projectPath+"/src/main/java");//路径需要自己配置
        gc.setAuthor("Chen");
        gc.setOpen(false);
        gc.setBaseResultMap(true);
        gc.setFileOverride(true);  // 是否覆盖
        gc.setServiceName("%sService"); // 去Service的I前缀
        gc.setIdType(IdType.ASSIGN_ID);
        gc.setDateType(DateType.ONLY_DATE);
        gc.setSwagger2(true);
        mpg.setGlobalConfig(gc);

        //2、设置数据源
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://192.168.4.120:3306/yz_cims?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("yangqing");
        dsc.setPassword("yangqing123");
        dsc.setDbType(DbType.MYSQL);
        mpg.setDataSource(dsc);

        //3、包的配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.framework.antong");
        pc.setModuleName("sys");
        pc.setEntity("entity");
        pc.setMapper("mapper");
        pc.setService("service");
        pc.setController("controller");
        mpg.setPackageInfo(pc);

        //4、策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setInclude("pea_cabinet"); // 设置要映射的表名
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        // 实体类的基础父类
        strategy.setSuperEntityClass("com.framework.antong.sys.bean.BaseEntity");
        strategy.setSuperEntityColumns("createBy","createTime","updateBy","updateTime","isActive");

        // 自动lombok
        strategy.setEntityLombokModel(true);
        strategy.setLogicDeleteFieldName("isActive");
        // 自动填充配置
//        TableFill createTime = new TableFill("create_time", FieldFill.INSERT);
//        TableFill isActive = new TableFill("is_active", FieldFill.INSERT);
//        TableFill updateTime = new TableFill("update_time", FieldFill.UPDATE);
//        TableFill createBy = new TableFill("create_by",FieldFill.INSERT);
//        TableFill updateBy = new TableFill("update_by",FieldFill.UPDATE);
//        List<TableFill> tableFills = new ArrayList<>();
//        tableFills.add(createTime);
//        tableFills.add(isActive);
//        tableFills.add(updateTime);
//        tableFills.add(createBy);
//        tableFills.add(updateBy);
//        strategy.setTableFillList(tableFills);
        // 乐观锁
//        strategy.setVersionFieldName("version");
//        strategy.setRestControllerStyle(true);
//        strategy.setControllerMappingHyphenStyle(true);
        // localhost:8080/hello_id_2

        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-rb");
                map.put("superColums", this.getConfig().getStrategyConfig().getSuperEntityColumns());
                this.setMap(map);
            }
        };
        // 调整 xml 生成目录演示
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig("/templates/mapper.xml.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return projectPath+"/src/main/resources/com/framework/antong/sys/mapper/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        // 调整 domain 生成目录演示
        focList.add(new FileOutConfig("/templates/entity.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                //输出的位置
                return  projectPath+"/src/main/java/com/framework/antong/sys/entity/" + tableInfo.getEntityName() + ".java";
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);


        TemplateConfig tc = new TemplateConfig();
        //自定义模版
        //tc.setController("/templates/btl/controller.java");
        tc.setEntity(null);
        // 关闭默认 xml 生成，调整生成 至 根目录
        tc.setXml(null);
        mpg.setTemplate(tc);
        //使用Velo模版引擎
        //mpg.setTemplateEngine(new BeetlTemplateEngine());
        mpg.setTemplateEngine(new VelocityTemplateEngine());

        mpg.setStrategy(strategy);
        mpg.execute();
        //执行
    }
}
