<script>
    /*********************变量定义区 begin*************/
    //行索引计数器
    // 如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;




    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/customer/listNormal.action',
        paramName: 'likeName',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 2,
        width: 'flex',
        noSuggestionNotice: '不存在，请重新输入！',
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
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_customer_name').val(suggestion.name);
        }
    };

    // 结算员名称
    var operatorNameAutoCompleteOption = {
        serviceUrl: '/comprehensiveFee/listOperatorByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'realName',
        showNoSuggestionNotice: true,
        minChars: 2,
        //width: 'flex',
        noSuggestionNotice: '结算员不存在',
        transformResult: function (result) {
            if(result.success){
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
            }else{
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            /*$('#show_operator_name').val(suggestion.realName);*/
        }
    };

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        initTotal();
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/comprehensiveFee/listByQueryParams.action',
            pageSize: parseInt(size)
        });
        _grid.on('load-success.bs.table', function () {
            $('[data-toggle="tooltip"]').tooltip()
        })
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    function initTotal() {
        var obj={};
        obj.orderStatus=$('#orderStatus').val();
        obj.customerId=$('#customerId').val();
        obj.customerCardNo=$('#show_customer_card').val();
        obj.operatorId=$('#operatorId').val();
        obj.code=$('#code').val();
        obj.operatorTimeStart=$('#createdStart').val();
        obj.operatorTimeEnd=$('#createdEnd').val();
        $.ajax({
            type: "POST",
            url: "/comprehensiveFee/selectCountAndTotal.action",
            data: JSON.stringify(obj),
            processData: false,
            contentType: false,
            async: true,
            success: function (res) {
                if (res.code == "200") {
                    $('#transactionsNumCount').html('<span>总笔数：'+res.data.transactionsNumCount+'</span>');
                    $('#transactionsTotal').html('<span>总金额：'+((res.data.transactionsTotal)/100).toFixed(2)+'</span>');
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
            title: '检测收费单',//对话框title
            className: 'dialog-left',
            onShowStart(){
                $('.modal').attr('data-drag', 'draged');
            },
            content: '${contextPath}/comprehensiveFee/add.html', //对话框内容，可以是 string、element，$object
            width: '880px',//宽度
            height: '550px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });

    }

    /**
     * 打开确认支付页面
     * */
    function verificationUsernamePassword(id) {
        diaPay = bs4pop.dialog({
            title: '支付确认',//对话框title
            className: 'dialog-right',
            content: '${contextPath}/comprehensiveFee/verificationUsernamePassword.action?id=' + id, //对话框内容，可以是 string、element，$object
            width: '400px',//宽度
            height: '400px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });

    }

    /**
     * 补打
     */
    function openPrintHandler() {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_orderStatus != 2) {
            bs4pop.alert('该单据当前状态不能进行补打操作！');
            return;
        }
        //查询comprehensiveFee单信息
        $.ajax({
            type: "POST",
            dataType: "json",
            url: '/comprehensiveFee/printOneById.action',
            data: {id: rows[0].id},
            success: function (data) {
                bui.loading.hide();
                let comprehensiveFee=data.data;
                if (data.code == '200') {
                    //查询最新总余额
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        url: '/comprehensiveFee/queryAccountBalance.action',
                        data: {customerCardNo: rows[0].customerCardNo},
                        success: function (res) {
                            bui.loading.hide();
                            if (res.code == '200') {
                                //调用c端打印
                                comprehensiveFee.balance = res.data.accountFund.balance;
                                callbackObj.printPreview(JSON.stringify(comprehensiveFee), "1","CheckRechargeDocument",0);
                            }
                        },
                        error: function () {
                            bui.loading.hide();
                            bs4pop.alert("打印失败!", {type: 'error'});
                        }
                    });
                }
            },
            error: function () {
                parent.bui.loading.hide();
                parent.bs4pop.alert("打印失败!", {type: 'error'});
            }
        });

    }

    /*
    * 调用撤销功能
    * */
    function openUpdateHandler() {
        let rows = _grid.bootstrapTable('getSelections');
        let createTime=new Date(rows[0].createdTime);
        let createTimeToLocal=createTime.toLocaleDateString();
        let todaysDate = new Date();
        let todaysDateToLocal=todaysDate.toLocaleDateString();
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_orderStatus != 2) {
            bs4pop.alert('该单据当前状态不能进行撤销操作！');
            return;
        }
        if(createTimeToLocal != todaysDateToLocal){
            bs4pop.alert('只有当天的结算单可以撤销');
            return;
        }
        bs4pop.confirm(" 确定撤销当前单据吗？", {title: "信息确认"}, function (sure) {
            if (sure) {
                dia = bs4pop.dialog({
                    title: '撤销校验',//对话框title
                    content: '${contextPath}/comprehensiveFee/revocatorPage.html?id=' + rows[0].id, //对话框内容，可以是 string、element，$object
                    width: '400px',//宽度
                    height: '400px',//高度
                    backdrop: 'static',
                    isIframe: true//默认是页面层，非iframe
                });
            }
        });
    }


    /**
     * 打开查看
     * @param id
     */
    function openViewHandler(id) {
        if (!id) {
            //获取选中行的数据
            let rows = _grid.bootstrapTable('getSelections');
            if (null == rows || rows.length == 0) {
                bs4pop.alert('请选中一条数据');
                return;
            }
            id = rows[0].id;
        }


        dia = bs4pop.dialog({
            title: '检测收费单详情',
            content: '/comprehensiveFee/view.html?id=' + id,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '60%',
            height: '95%',
            btns: [{
                label: '关闭', className: 'btn-secondary', onClick(e) {
                }
            }]
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
     * 虚浮处理
     */
    function suspensionFun(value, row, index, field) {
        return "<span data-toggle='tooltip' data-placement='left' data-original-title='" + value + "'>" + value + "</span>";
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

    /** 点击编号查看事件*/
    function view(value, row, index) {
        return [
            '<a href=javascript:openViewHandler('+row.id+')>'+value+'</a>'
        ].join("")
    }
    /*****************************************函数区 end**************************************/

</script>