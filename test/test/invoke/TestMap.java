package test.invoke;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestMap {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "one");
        System.out.println(map.compute("2", (k, v) -> v != null ? v : k));
        System.out.println(map);
    }

}
