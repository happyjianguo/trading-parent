<script>
    /*********************变量定义区 begin*************/
    //行索引计数器
    //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;
    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    /**
     * 页面初始化加载树形结构数据
     */
    $(function(){
        var requestUrl = "${ctxPath}/goodsReferencePriceSetting/getAllGoods.action";

        var setting  = {
            view: {
                //addHoverDom:addHoverDom,
                //removeHoverDom:removeHoverDom,
                selectedMulti: false,
                showIcon:false
            },
            edit: {
                enable: true,
                showRenameBtn: false,
                showRemoveBtn: false
            },
            data : {
                key:{
                    name:"name"
                },
                simpleData : {
                    enable : true,
                    idKey  : "id",
                    pIdKey : "parent"
                }
            },
            callback: {
                onClick: zTreeOnClick
            }
        };

        $.ajax({
            url : requestUrl,
            dataType : 'json',
            type : 'post',
            async : false,
            success : function(data) {
                var treeObj =  $.fn.zTree.init($("#goodsTree"), setting , data);
                treeObj.expandAll(false);
            }
        });

        function filter(treeId, parentNode, childNodes) {
            if (!childNodes.data) return null;
            for (var i=0, l=childNodes.data.length; i<l; i++) {
                childNodes.data[i].name = childNodes.data[i].name.replace('','');
            }
            return childNodes.data;
        };
    });


    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 点击树形节点加载右侧列表数据
     */
    function zTreeOnClick(event, menuTree, treeNode)
    {
        document.getElementById("tableDiv").style.display = "block";
        var oid = treeNode.id;
        $("input[name='parentGoodsId']").val(oid);
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let tableOptions = createTableOptions('/goodsReferencePriceSetting/getGoodsByParentId.action');
        _grid.bootstrapTable('refreshOptions', tableOptions);
    }


    /**
     * 打开新增/修改窗口
     */
    function edit(goodsId) {
        dia = bs4pop.dialog({
            title: '新增价格预警',//对话框title
            content: '${contextPath}/goodsReferencePriceSetting/add.html?goodsId=' + goodsId,
            width: '60%',//宽度
            height: '85%',//高度
            isIframe: true,//默认是页面层，非iframe
            btns: [
                {
                    label: '确定', className: 'btn btn-primary', onClick(e, $iframe) {
                        let diaWindow = $iframe[0].contentWindow;
                        bui.util.debounce(diaWindow.saveOrUpdateHandler, 1000, true)()
                        return false;
                    }
                }, {
                    label: '取消', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]
        });

    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        let val = $("input[name='onlyExistReferencePrice']").prop("checked")
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        };
        let extend = $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
        extend.onlyExistReferencePrice = val ? 1 : 0;
        return extend;
    }

    /**
     * 查询处理(根据快捷吗查询商品)
     */
    function queryDataHandler() {
        document.getElementById("tableDiv").style.display = "block";
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let tableOptions = createTableOptions('/goodsReferencePriceSetting/getGoodsByKeyword.action');
        _grid.bootstrapTable('refreshOptions', tableOptions);
        _grid.bootstrapTable('refresh');
    }
    /*****************************************函数区 end**************************************/

    function createTableOptions(url) {
        return {
            url: url,
            columns: [
                {
                    field: 'goodsName',
                    title: '品类名称 ',
                    align: 'center'
                }, {
                    field: 'genericItem.referenceRuleText',
                    title: '常规交易参考价规则',
                    align: 'center',
                    formatter: function (value, row) {
                        return formatReferenceRule(value, row)
                    }
                }, {
                    field: 'traditionFarmerItem.referenceRuleText',
                    title: '老农交易参考价规则',
                    align: 'center',
                    formatter: function (value, row) {
                        return formatReferenceRule(value, row)
                    }
                }, {
                    field: 'selfItem.referenceRuleText',
                    title: '自营交易参考价',
                    align: 'center',
                    formatter: function (value, row) {
                        return formatReferenceRule(value, row)
                    }
                }, {
                    field: 'oid',
                    title: '操作',
                    align: 'center',
                    formatter: function (value, row, index) {
                        var actions = [];
                        actions.push('<a href="#" onclick="edit(\'' + row.goodsId + '\')"><i class="fa fa-edit"></i>编辑</a> ');
                        return actions.join('');
                    }
                }
            ],
            responseHandler: function (res) {
                if (res.success) {
                    return res.data;
                } else {
                    bs4pop.alert(res.message, {type: 'error'});
                    return null;
                }
            }
        };
    }

    function formatReferenceRule(value, row) {
        if (value == undefined) {
            return null;
        }
        let val = value.split("：");
        if (row.referenceRule == 3) {
            return val[0] + "：" + row.fixedPriceText
        }
        return val[0];
    }
</script>
