import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

public class test
{
    @Test
    public void test1()
    {
        long id1 = IdWorker.getId();
        long id2 = IdWorker.getId();
        System.out.println(id1);
        System.out.println(id2);
    }
}
