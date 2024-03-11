import com.enaveng.model.MainTemplatesConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;


public class MainTemplatesTest {
    @Test
    public void test() throws Exception {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        //解决 FreeMarker 打印奇怪的数字格式 (比如 1,000,000 或 1 000 000 而不是 1000000)
        configuration.setNumberFormat("0.######");

        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate("MainTemplates.java.ftl");

        // 创建数据模型
        MainTemplatesConfig mainTemplatesConfig = new MainTemplatesConfig();
        mainTemplatesConfig.setLoop(false);
        mainTemplatesConfig.setAuthor("dlwlrma");
        mainTemplatesConfig.setOutputText("得到的结果为:");


        // 生成对应的文件
        Writer out = new FileWriter("MainTemplates.java");
        template.process(mainTemplatesConfig, out);

        // 生成文件后关闭
        out.close();
    }

}
