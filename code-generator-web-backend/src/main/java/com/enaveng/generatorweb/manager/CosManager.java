package com.enaveng.generatorweb.manager;

import com.enaveng.generatorweb.common.ErrorCode;
import com.enaveng.generatorweb.config.CosClientConfig;
import com.enaveng.generatorweb.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    //防止每一次请求时都去创建线程池
    private TransferManager transferManager;

    //在bean初始化完成之后执行
    @PostConstruct
    private void init() {
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        transferManager = new TransferManager(cosClient, threadPool);
    }

    /**
     * 上传对象
     *
     * @param key           对象键(Key)是对象在存储桶中的唯一标识。
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  对象键(Key)是对象在存储桶中的唯一标识。 也就是上传到的文件目录
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 下载文件
     *
     * @param key 唯一标识
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 从对象存储上下载文件到本地
     *
     * @param key           下载的文件
     * @param localFilePath 下载到的本地文件路径
     * @return
     * @throws InterruptedException
     */
    public Download download(String key, String localFilePath) throws InterruptedException {
        //本地文件路径
        File downloadFile = new File(localFilePath);
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        // 返回一个异步结果 Download, 可同步的调用 waitForCompletion 等待下载结束, 成功返回 void, 失败抛出异常
        Download download = transferManager.download(getObjectRequest, downloadFile, true);
        download.waitForCompletion();
        return download;
    }


    public void createBucket() {
        String bucket = "examplebucket-1250000000"; //存储桶名称，格式：BucketName-APPID
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
        // 设置 bucket 的权限为 Private(私有读写)、其他可选有 PublicRead（公有读私有写）、PublicReadWrite（公有读写）
        createBucketRequest.setCannedAcl(CannedAccessControlList.Private);
        try {
            Bucket bucketResult = cosClient.createBucket(createBucketRequest);
        } catch (CosServiceException serverException) {
            serverException.printStackTrace();
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
        }
    }

    /**
     * 删除对象
     *
     * @param key
     * @throws CosClientException
     * @throws CosServiceException
     */
    public void deleteObject(String key) throws CosClientException, CosServiceException {
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除指定对象失败");
        }
    }

    /**
     * 批量删除对象
     *
     * @param keyList
     * @return
     */
    public DeleteObjectsResult deleteObjects(List<String> keyList) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        // 设置要删除的key列表, 最多一次删除1000个
        ArrayList<DeleteObjectsRequest.KeyVersion> keyVersionList = new ArrayList<>();
        // 传入要删除的文件名
        // 注意文件名不允许以正斜线/或者反斜线\开头，例如：
        // 存储桶目录下有a/b/c.txt文件，如果要删除，只能是 keyList.add(new KeyVersion("a/b/c.txt")), 若使用 keyList.add(new KeyVersion("/a/b/c.txt"))会导致删除不成功
        for (String key : keyList) {
            keyVersionList.add(new DeleteObjectsRequest.KeyVersion(key));
        }
        deleteObjectsRequest.setKeys(keyVersionList);
        DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
        return deleteObjectsResult;
    }

    /**
     * 删除目录
     *
     * @param delDir 相对 bucket 的路径
     */
    public void deleteDir(String delDir) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置 bucket 名称
        listObjectsRequest.setBucketName(cosClientConfig.getBucket());
        // prefix 表示列出的对象名以 prefix 为前缀
        // 这里填要列出的目录是相对 bucket 的路径
        listObjectsRequest.setPrefix(delDir);
        // 设置最大遍历出多少个对象, 一次 listobject 最大支持1000
        listObjectsRequest.setMaxKeys(1000);
        // 保存每次列出的结果
        ObjectListing objectListing = null;
        do {
            objectListing = cosClient.listObjects(listObjectsRequest);
            // 这里保存列出的对象列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            ArrayList<DeleteObjectsRequest.KeyVersion> delObjects = new ArrayList<>();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                delObjects.add(new DeleteObjectsRequest.KeyVersion(cosObjectSummary.getKey()));
            }
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
            deleteObjectsRequest.setKeys(delObjects);
            cosClient.deleteObjects(deleteObjectsRequest);
            // 标记下一次开始的位置
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        }
        while (objectListing.isTruncated());
    }
}
