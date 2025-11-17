$(function () {
    $("#jqGrid").jqGrid({
        url: '/admin/goods/list',
        datatype: "json",
        colModel: [
            {label: '商品编号', name: 'goodsId', index: 'goodsId', width: 60, key: true},
            {label: '商品货号', name: 'goodsNum', index: 'goodsNum', width: 60},
            {label: '商品名', name: 'goodsName', index: 'goodsName', width: 100},
            {label: '商品简介', name: 'goodsIntro', index: 'goodsIntro', width: 120},
            {label: '商品图片', name: 'goodsCoverImg', index: 'goodsCoverImg', width: 120, formatter: coverImageFormatter},
            {label: '商品库存', name: 'stockNum', index: 'stockNum', width: 60},
            {label: '商品售价', name: 'sellingPrice', index: 'sellingPrice', width: 60},
            {
                label: '上架状态',
                name: 'goodsSellStatus',
                index: 'goodsSellStatus',
                width: 80,
                formatter: goodsSellStatusFormatter
            },
            {label: '包装单位', name: 'packagingUnit', index: 'packagingUnit', width: 60},
            {label: '规格', name: 'specifications', index: 'specifications', width: 60},
            {label: '供货周期', name: 'supplyCycle', index: 'supplyCycle', width: 60, formatter: goodsSupplyCycleMatter},
            {label: '创建时间', name: 'createTime', index: 'createTime', width: 60}
        ],
        height: 760,
        rowNum: 20,
        rowList: [20, 50, 80],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "data.list",
            page: "data.currPage",
            total: "data.totalPage",
            records: "data.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order",
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    $(window).resize(function () {
        $("#jqGrid").setGridWidth($(".card-body").width());
    });

    function goodsSellStatusFormatter(cellvalue) {
        //商品上架状态 0-上架 1-下架
        if (cellvalue == 0) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">销售中</button>";
        }
        if (cellvalue == 1) {
            return "<button type=\"button\" class=\"btn btn-block btn-secondary btn-sm\" style=\"width: 80%;\">已下架</button>";
        }
    }

    function goodsSupplyCycleMatter(cellvalue) {
        //1	现货
        // 2	1-2天
        // 3	3-5天
        // 4	1周
        // 5	1-2周
        // 6	3-4周
        // 7	电询
        if (cellvalue == 1) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">现货</button>";
        } else if (cellvalue == 2) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">1-2天</button>";
        } else if (cellvalue == 3) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">3-5天</button>";
        } else if (cellvalue == 4) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">1周</button>";
        } else if (cellvalue == 5) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">1-2周</button>";
        } else if (cellvalue == 6) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">3-4周</button>";
        } else if (cellvalue == 7) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">电询</button>";
        } else {
            return "<button type=\"button\" class=\"btn btn-block btn-secondary btn-sm\" style=\"width: 80%;\">其他</button>";
        }
    }

    function coverImageFormatter(cellvalue) {
        return "<img src='" + cellvalue + "' height=\"80\" width=\"80\" alt='商品主图'/>";
    }

});

/**
 * jqGrid重新加载
 */
function reload() {
    initFlatPickr();
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}

/**
 * 添加商品
 */
function addGoods() {
    window.location.href = "/admin/goods/edit";
}

/**
 * 修改商品
 */
function editGoods() {
    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    window.location.href = "/admin/goods/edit/" + id;
}

function searchGoods() {
    // 商品名称
    var goodsName = $('#goodsNameInput').val();
    var goodsSellStatus = $('#goodsStatusSelect').val();
    if (goodsSellStatus == 100) {
        //全部则传空
        goodsSellStatus = '';
    }
    // 数据封装
    var searchData = {goodsName: goodsName, goodsSellStatus: goodsSellStatus};
    // 传入查询条件参数
    $("#jqGrid").jqGrid("setGridParam", {postData: searchData});
    // 点击搜索按钮默认都从第一页开始
    $("#jqGrid").jqGrid("setGridParam", {page: 1});
    // 提交post并刷新表格
    $("#jqGrid").jqGrid("setGridParam", {url: 'goods/list'}).trigger("reloadGrid");
}

/**
 * 上架
 */
function putUpGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行上架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: "/admin/goods/status/0",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.resultCode == 200) {
                            swal("上架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    )
    ;
}

function exportGoods() {
    console.log("exportGoods函数被调用");
    
    var fileInput = $("#articleImageFile")[0];
    console.log("文件输入元素:", fileInput);
    
    var file = fileInput.files[0];
    console.log("选中的文件:", file);
    
    if (!file) {
        swal("提示", "请选择要上传的Excel文件", "warning");
        return;
    }
    
    // 检查文件格式
    var fileName = file.name;
    console.log("文件名:", fileName);
    
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
        swal("错误", "请选择Excel文件格式（.xlsx或.xls）", "error");
        return;
    }
    
    // 检查文件大小（限制为10MB）
    if (file.size > 10 * 1024 * 1024) {
        swal("错误", "文件大小不能超过10MB", "error");
        return;
    }
    
    var formData = new FormData();
    formData.append("file", file);
    console.log("FormData已创建，准备发送请求");
    
    // 显示加载提示
    swal({
        title: "正在处理",
        text: "正在上传并处理Excel文件，请稍候...",
        icon: "info",
        buttons: false,
        closeOnClickOutside: false,
        closeOnEsc: false
    });
    
    console.log("开始发送AJAX请求到:", "/admin/goods/export");
    
    $.ajax({
        url: "/admin/goods/export",
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        timeout: 60000, // 60秒超时
        beforeSend: function(xhr) {
            console.log("AJAX请求开始发送");
        },
        success: function (response) {
            console.log("收到成功响应:", response);
            if (response.resultCode == 200) {
                swal("成功", "商品数据导入成功！", "success");
                // 清空文件输入框
                $("#articleImageFile").val("");
                // 刷新商品列表
                $("#jqGrid").trigger("reloadGrid");
            } else {
                swal("失败", response.message || "导入失败", "error");
            }
        },
        error: function (xhr, status, error) {
            console.log("AJAX请求失败:", {xhr: xhr, status: status, error: error});
            var errorMsg = "上传失败";
            if (status === "timeout") {
                errorMsg = "请求超时，请检查文件大小或稍后重试";
            } else if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMsg = xhr.responseJSON.message;
            } else if (xhr.status) {
                errorMsg = "HTTP错误: " + xhr.status + " - " + xhr.statusText;
            }
            swal("错误", errorMsg, "error");
        }
    });
}

/**
 * 下架
 */
function putDownGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行下架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: "/admin/goods/status/1",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.resultCode == 200) {
                            swal("下架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    );


}