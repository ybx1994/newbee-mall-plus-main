

/**
 * 导出商品
 */
function liDeExportGoods() {
    // 显示导出状态
    $("#exportStatus").show();

    // 使用XMLHttpRequest方式下载文件
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/lide/goods/liDeExportGoods', true);
    xhr.responseType = 'blob';

    xhr.onload = function() {
        if (xhr.status === 200) {
            // 隐藏导出状态
            $("#exportStatus").hide();

            // 创建下载链接
            var blob = xhr.response;
            var downloadUrl = window.URL.createObjectURL(blob);
            var a = document.createElement("a");
            a.href = downloadUrl;
            a.download = "理德东方商品数据.xlsx";
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(downloadUrl);
        } else {
            // 隐藏导出状态
            $("#exportStatus").hide();
            alert("导出失败，请重试");
        }
    };

    xhr.onerror = function() {
        // 隐藏导出状态
        $("#exportStatus").hide();
        alert("导出失败，请检查网络连接");
    };

    // 发送请求
    xhr.send();
}

/**
 * 导出所有商品
 */
function liDeExportGoodsAll() {
    // 显示导出状态
    $("#exportStatus").show();

    // 使用XMLHttpRequest方式下载文件
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/lide/goods/liDeExportGoodsAll', true);
    xhr.responseType = 'blob';

    xhr.onload = function() {
        if (xhr.status === 200) {
            // 隐藏导出状态
            $("#exportStatus").hide();

            // 创建下载链接
            var blob = xhr.response;
            var downloadUrl = window.URL.createObjectURL(blob);
            var a = document.createElement("a");
            a.href = downloadUrl;
            a.download = "理德东方全部商品数据.xlsx";
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(downloadUrl);
        } else {
            // 隐藏导出状态
            $("#exportStatus").hide();
            alert("导出失败，请重试");
        }
    };

    xhr.onerror = function() {
        // 隐藏导出状态
        $("#exportStatus").hide();
        alert("导出失败，请检查网络连接");
    };

    // 发送请求
    xhr.send();
}
