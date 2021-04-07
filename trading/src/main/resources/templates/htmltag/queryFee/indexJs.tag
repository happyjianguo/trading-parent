<script>
    // 客户名称(客户编号/客户名称/联系方式，联想查询)
    var customerNameAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 2,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if (result.success) {
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.code + ' | ' + dataItem.name + ' | ' + dataItem.contactsPhone
                            }
                        );
                    })
                }
            } else {
                // bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_customer_name').val(suggestion.name);
        }
    };

    // 结算员名称(用户工号（用户名），及姓名查询)
    var operatorNameAutoCompleteOption = {
        serviceUrl: '/comprehensiveFee/listOperatorByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'realName',
        showNoSuggestionNotice: true,
        noSuggestionNotice: '结算员不存在',
        transformResult: function (result) {
            if (result.success) {
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                //value: dataItem.realName + '（' + dataItem.serialNumber + '）'
                                value: dataItem.userName + '|' + dataItem.realName
                            }
                        );
                    })
                }
            } else {
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            /*$('#show_operator_name').val(suggestion.realName);*/
        }
    };

    // 卡号(12位卡号后，精确匹配)
    var customerCardQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if (result.success) {
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.customerName + "|" + dataItem.cardNo
                            }
                        );
                    })
                    //888810054629
                }
            } else {
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_customer_card_no').val(suggestion.customerName);
            $('#customerCardNo').val(suggestion.cardNo);
        }
    };


    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    /**
     * 页面初始化加载
     */
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        initTotal();
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/queryFee/listByQueryParams.action',
            //onLoadSuccess: toFixChargeAmount,
            pageSize: parseInt(size)
        });
    });

    /*执行前改变数据*/
    function toFixChargeAmount(data) {
        var rows = data.rows;
        for (var i = 0; i < rows.length; i++) {
            var chargeAmount = rows[i].chargeAmount;
            rows[i].chargeAmount = (chargeAmount / 100).toFixed(2);
        }
        _grid.bootstrapTable('load', data);
    }

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/

    function reprintHandler() {
        // 获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        var url = '';
        var serialNo = null;
        var id = null;
        url = '/queryFee/getPrintData.action'
        id = rows[0].id;
        // 先拿到票据信息，在调用c端打印
        $.ajax({
            type: "POST",
            // dataType: "json",
            url: url,
            data: {id: id},
            success: function (data) {
                bui.loading.hide();
                if (data.code == '200') {
                    // 调用c端打印
                    callbackObj.printPreview(JSON.stringify(data.data), "1", "ComprehensiveFeeDocument", 0);
                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("打印失败!", {type: 'error'});
            }
        });
    }


    function initTotal() {
        var obj = {};
        obj.customerId = $('#customerId').val();
        obj.customerCardNo = $('#show_customer_card').val();
        obj.operatorId = $('#operatorId').val();
        obj.operatorTimeStart = $('#createdStart').val();
        obj.operatorTimeEnd = $('#createdEnd').val();
        $.ajax({
            type: "POST",
            url: "/queryFee/selectCountAndTotal.action",
            data: JSON.stringify(obj),
            processData: false,
            contentType: false,
            async: true,
            success: function (res) {
                if (res.code == "200") {
                    $('#transactionsNumCount').html('<span>总笔数：' + res.data.transactionsNumCount + '</span>');
                    $('#transactionsTotal').html('<span>总金额：' + ((res.data.transactionsTotal) / 100).toFixed(2) + '</span>');
                }
            },
            error: function (error) {
                bs4pop.alert(error.message, {type: 'error'});
            }
        });
    }

    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        diaAdd = bs4pop.dialog({
            title: '查询收费',//对话框title
            className: 'dialog-left',
            onShowStart() {
                $('.modal').attr('data-drag', 'draged');
            },
            content: '${contextPath}/queryFee/add.html', //对话框内容，可以是 string、element，$object
            width: '880px',//宽度
            height: '350px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });
    }

    /**
     * 打开确认支付页面
     */
    function verificationUsernamePassword(id) {
        diaPay = bs4pop.dialog({
            title: '支付确认',//对话框title
            className: 'dialog-right',
            content: '${contextPath}/queryFee/verificationUsernamePassword.action?id=' + id, //对话框内容，可以是 string、element，$object
            width: '400px',//宽度
            height: '400px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });
    }

    /**
     * 查询处理
     */
    function queryDataHandler() {
        initTotal();
        _grid.bootstrapTable('refresh');
    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        };
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }

    /*****************************************函数区 end**************************************/

</script>