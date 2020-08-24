package temp_hashtable;

import java.util.Arrays;

/**
 * hash 表数据结构实例详解
 * <pre>
 * 在哈希表中，数据以数组格式存储，其中每个数据值都有自己唯一的索引
 * 哈希是一种将一系列键值转换成数组的一系列索引的技术
 * </pre>
 * @author BYRON.Y.Y
 *
 */
public class HashTableExample {

    private static int SIZE = 20;//function: mode

    private static DataItem[] hashArray = new DataItem[20];

    public static void main(String[] args) {
        System.out.println(Arrays.asList(hashArray));

        insert(new DataItem(100, 1));
        insert(new DataItem(100, 2));
        insert(new DataItem(100, 102));
        insert(new DataItem(100, 122));
        insert(new DataItem(100, 42));
        insert(new DataItem(100, 82));
        insert(new DataItem(100, 62));
        insert(new DataItem(100, 142));

        insert(new DataItem(100, 162));
        insert(new DataItem(100, 182));
        insert(new DataItem(100, 202));
        insert(new DataItem(100, 222));
        insert(new DataItem(100, 242));
        insert(new DataItem(100, 282));
        insert(new DataItem(100, 262));
        insert(new DataItem(100, 302));

        insert(new DataItem(100,322));
        insert(new DataItem(100, 342));
        insert(new DataItem(100,362));
        insert(new DataItem(100, 382));

        System.out.println(Arrays.asList(hashArray));
    }



    static class DataItem{
        int data;
        int key;
        public DataItem(int data, int key) {
            super();
            this.data = data;
            this.key = key;
        }
        @Override
        public String toString() {
            return "DataItem [key=" + key + "]";
        }

    }

    /**简易Hash方法，取模*/
    static int hashCode(int key){
           return key % SIZE;
    }

    /**
     * 发生hash冲突时，检索效率就会低了，所以hash算法很关键，唯一的最好
     * @param key
     * @return
     */
    static DataItem search(int key) {
        int idx = hashCode(key);

        while(null != hashArray[idx]) {
            // （即哈希值已经被占用了）

            if(hashArray[idx].key == key) {
                //如果hash相同的索引key值一样则检索到了
                return hashArray[idx];
            }

            //hash冲突，没有检索到，则线性推测查找
            idx++;

            //环绕检索,下一个索引位置
            idx = hashCode(idx);
        }

        return null;
    }


    /**
     * 插入新值到hash表
     * <pre>
     * hash后，发现坑位没有被占，则占有该坑位；
     * hash后，发现该坑位被占了：
     *      Step1: key和新插入的key不等，则使用线性推测方式，按队列顺序往后打探下一个坑位的情况（这是一个循环表顺序）
     *      Step2： 循环打探坑位：发现新坑位则占住
     * </pre>
     * @param item
     */
    static void insert(DataItem item) {
        boolean standFlag = false;
        //获取hash索引
        int idx = hashCode(item.key);

        if( null != hashArray[idx]  ) {
            if( hashArray[idx].key != item.key ) {
                //【被占】---》 则使用线性推测方式处理，找到空位进入
                while( null != hashArray[idx] && hashArray[idx].key != item.key ) {

                    idx = hashCode( ++idx );
                    if( null == hashArray[idx] ) {
                        hashArray[idx] = item;
                        standFlag = true;
                    }
                }
            }


        }else {
            //hash索引处为empty，则坑可用，直接存储
            hashArray[idx] = item;
            standFlag = true;
        }

        if( !standFlag ) {
            System.err.println("插入失败！");
        }
    }
}