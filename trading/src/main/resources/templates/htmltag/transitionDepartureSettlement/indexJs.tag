<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia, diaAdd, diaPay;


    //时间范围
    lay('.settletime').each(function () {
        laydate.render({
            elem: this
            ,theme: '#007bff'
            , trigger: 'click'
            , range: false
            , type: 'date'
            // , min: getLastYearYestdy(new Date())
            , max: timeStamp2String(new Date().getTime()),
            done: function (value, date) {
                isStartEndDatetime(this.elem, value);
            }
        });
    });

    function swipeCard(el) {
        let cardNo;
        let json = JSON.parse(callbackObj.readCardNumber());
        if (json.code == 0) {
            cardNo = json.data;
        } else {
            bs4pop.alert(json.message, {type: "error"});
            return false;
        }
        $('#show_customer_card').val(cardNo);
        $.ajax({
            type: "POST",
            dataType: "json",
            url: '/customer/listCustomerByCardNo.action',
            data: {cardNo: cardNo},
            success: function (data) {
                if (data.code == '200') {
                    $('#show_customer_name').val(data.data[0].name);
                    $('#accountId').val(data.data[0].id);
                } else {
                    $('#show_customer_name').val('');
                    bs4pop.alert(data.result, {type: 'error'});
                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("客户获取失败!", {type: 'error'});
            }
        });
    }

    function getLastYearYestdy(date) {
        var strYear = date.getFullYear() - 1;
        var strDay = date.getDate();
        var strMonth = date.getMonth() + 1;
        if (strMonth < 10) {
            strMonth = "0" + strMonth;
        }
        if (strDay < 10) {
            strDay = "0" + strDay;
        }
        return strYear + "-" + strMonth + "-" + strDay;
    }

    function timeStamp2String(time) {
        var datetime = new Date();
        datetime.setTime(time);
        var year = datetime.getFullYear();
        var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
        var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
        return year + "-" + month + "-" + date;
    }


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
            $('#show_customer_name').val(suggestion.customerName);
            $('#customerCardNo').val(suggestion.cardNo);
        }
    };


    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/customer/listNormal.action',
        paramName: 'likeName',
        displayFieldName: 'code',
        width: '320px',
        showNoSuggestionNotice: true,
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
            $('#showCustomerName').val(suggestion.name);
        }
    };

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/transitionDepartureSettlement/listByQueryParams.action',
            pageSize: parseInt(size)
        });
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        diaAdd = bs4pop.dialog({
            title: '转离场结算单',//对话框title
            className: 'dialog-left',
            onShowStart(){
                $('.modal').attr('data-drag', 'draged');
            },
            content: '${contextPath}/transitionDepartureSettlement/add.html', //对话框内容，可以是 string、element，$object
            width: '880px',//宽度
            height: '540px',//高度
            isIframe: true,//默认是页面层，非iframe
            backdrop: 'static',

        });

    }

    function verificationUsernamePassword(id) {
        diaPay = bs4pop.dialog({
            title: '支付确认',//对话框title
            className: 'dialog-right',
            content: '${contextPath}/transitionDepartureSettlement/verificationUsernamePassword.action?id=' + id, //对话框内容，可以是 string、element，$object
            width: '400px',//宽度
            height: '400px',//高度
            isIframe: true,//默认是页面层，非iframe
            backdrop: 'static'
        });

    }

    /**
     打开更新窗口:iframe
     */
    function openUpdateHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        if (rows[0].$_payStatus != 2) {
            bs4pop.alert('只有已结算的单子可以撤销');
            return false;
        }
        let flag=false;
        $.ajax({
            type: "POST",
            url: "/transitionDepartureSettlement/getOneById.action?id="+rows[0].id,
            processData: false,
            contentType: false,
            async: false,
            success: function (res) {
                bui.loading.hide();
                if (res.code == "200") {
                    flag=true;
                } else {
                    bs4pop.alert(res.message, {width:'350px',height:"200px",type: 'error'});
                }
            },
            error: function (error) {
                bs4pop.alert(error.message, {width:'350px',height:"200px",type: 'error'});
            }
        });
        if(flag){
            dia = bs4pop.dialog({
                title: '撤销校验',//对话框title
                className: 'modal-fixed',
                content: '${contextPath}/transitionDepartureSettlement/revocatorPage.html?id=' + rows[0].id,
                width: '400px',//宽度
                height: '400px',//高度
                isIframe: true,//默认是页面层，非iframe
                backdrop: 'static',
                btns: [{
                    label: '确定', className: 'btn btn-primary', onClick(e, $iframe) {
                        let diaWindow = $iframe[0].contentWindow;
                        bui.util.debounce(diaWindow.revocator, 1000, true)()
                        return false;
                    }
                },{
                    label: '返回', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]

            });
        }
    }

    function openPrintHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        if (rows[0].$_payStatus != 2) {
            bs4pop.alert('只有已结算的单子可以补打');
            return false;
        }
        //先拿到票据信息，在调用c端打印
        $.ajax({
            type: "POST",
            dataType: "json",
            url: '/transitionDepartureSettlement/getOneById.action',
            data: {id: rows[0].id},
            success: function (data) {
                bui.loading.hide();
                if (data.code == '200') {
                    //调用c端打印
                    callbackObj.printPreview(JSON.stringify(data.data),"1","TransitionDocument",1);
                }else{
                    bs4pop.alert(data.message, {type: 'error'});
                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("打印失败!", {type: 'error'});
            }
        });


    }

    function openDeleteHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        bs4pop.confirm("确定要删除吗", {title: "确认提示"}, function (sure) {
            if (sure) {
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: '/carTypePublic/delete.action',
                    data: {id: rows[0].id},
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            return;
                        }
                        // bs4pop.alert('成功', {type: 'success '}, function () {
                        //     window.location.reload();
                        // });
                        window.location.reload();
                    },
                    error: function () {
                        bui.loading.hide();
                        bs4pop.alert("车型删除失败!", {type: 'error'});
                    }
                });
            }
        });
    }

    /**
     * 禁启用操作
     * @param enable 是否启用:true-启用
     * @param id
     */
    /* function doEnableHandler(enable, id) {
         var opType ="";
         if(enable == 1){
             opType = "enable";
         }
         if(enable == 2){
             opType = "disable";
         }
         let rows = _grid.bootstrapTable('getSelections');
         if (null == rows || rows.length == 0) {
             bs4pop.alert('请选中一条数据');
             return;
         }
         //table选择模式是单选时可用
         let msg = enable == 1 ? '确定要启用该车型吗？' : '确定要禁用该车型吗？';
         bs4pop.confirm(msg, undefined, function (sure) {
             if (sure) {
                 bui.loading.show('努力提交中，请稍候。。。');
                 $.ajax({
                     type: "POST",
                     url: "${contextPath}/carTypePublic/updateStatus.action",
                    data: {id: rows[0].id, status: enable,opType:opType, "carType.name": rows[0].carTypeName},
                    processData: true,
                    dataType: "json",
                    async: true,
                    success: function (data) {
                        bui.loading.hide();
                        if (data.success) {
                            _grid.bootstrapTable('refresh');
                            $("#btn_disabled").removeClass("btn_css_disabled");
                            $("#btn_enabled").removeClass("btn_css_disabled");
                            _modal.modal('hide');
                        } else {
                            bs4pop.alert(data.result, {type: 'error'});
                        }
                    },
                    error: function () {
                        bui.loading.hide();
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    }
                });
            }
        })
    }*/


    /**
     * 查询处理
     */
    function queryDataHandler() {
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