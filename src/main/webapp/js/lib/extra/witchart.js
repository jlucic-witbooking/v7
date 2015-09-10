
/*TODO: REPLACE JQUERY*/
/*TODO: FIX NEXT AND PREVIOUS BUTTONS*/

/*
 * args.wrapper     element where to print graph
 * args.values      data to plot on graph
 *
 */

var graphMaker = function (args) {
  if (!args.wrapper || !args.values) {
    return false;
  }
  // var data = google.visualization.arrayToDataTable(args.values);
    var data = new google.visualization.DataTable();
    data.addColumn('string', args.values[0][0]);
    data.addColumn('number', args.values[0][1]);
    data.addColumn({type: 'string', role: 'tooltip'});
    data.addColumn({type: 'string',role: 'style'});
    data.addColumn({type: 'string',role: 'class'});
    data.addRows(args.values.slice(1,args.values.length));
  var width;
  var chart;
  var options = {    
    animation: {
      duration: 1000,
      easing: 'easyOut'
    }, 
    bar: {
      groupWidth: '75%'
    }, 
    hAxis: {
      baselineColor: '#dcdcdc',
      format: '#',
      gridlines: {
        color: '#E4E4E4',
        count: 5
      },
      textStyle: {
        color: '#565656',
        fontSize: 10
      }
    },
    chartArea: {
      width: '85%',
      height:'80%'
    },
    vAxis: {
      baselineColor: '#dcdcdc',
      format: '#'+args.coin,
      gridlines: {
        color: '#E4E4E4',
        count: 5
      },
    /*TODO: HACK TO REMOVE PRICES TEMPORARILY REMOVEEEEEEEEEEEEEEEEE!!*/
/*    baselineColor: '#fff',
    gridlineColor: '#fff',
    textPosition: 'none',*/
      textStyle: {
        color: '#565656',
        fontSize: 12
      }
    },
    colors: ['green', 'green'],
    legend: {position: 'none'}
  };
  var optionsMobile = {
    tooltip: { trigger: 'selection' },
    animation: {
      duration: 1000,
      easing: 'easyOut'
    }, 
    bar: {
      groupWidth: '75%'
    },
    hAxis: {
      baselineColor: '#dcdcdc',
      format: '#',
      gridlines: {
        color: '#E4E4E4',
        count: 5
      },
      textStyle: {
        color: '#565656',
        fontSize: 6
      }
    },
    chartArea: {
      width: '85%',
      height:'80%'
    },
    vAxis: {
      baselineColor: '#dcdcdc',
      format: '#'+args.coin,
      gridlines: {
        color: '#E4E4E4',
        count: 5
      },
      textStyle: {
        color: '#565656',
        fontSize: 10
      }
    },
    colors: ['green', 'green'],
    legend: {position: 'none'}
  };
  if (args.maxValue) options.vAxis.maxValue = args.maxValue;
  if (args.minValue) options.vAxis.minValue = args.minValue;
  var createChart = function () {
    chart? chart.clearChart():undefined;
    if (width < 500) {
      createBarChart();
    } else {
      createColumnChart()
    }
  };
  var createBarChart = function () {
    $(args.wrapper).height(600);
    chart = new google.visualization.BarChart(args.wrapper);
    chart.draw(data, optionsMobile);
  };
  var createColumnChart = function () {
    $(args.wrapper).height(200);
    chart = new google.visualization.ColumnChart(args.wrapper);
    chart.draw(data, options);
  };
  var removeChart = function () {
    chart.clearChart();
    width = undefined;
  };
  var render = function () {
    var currentWidth = $(args.wrapper).width();
    if (width !== currentWidth) {
      width = currentWidth;
      createChart();
    }
  };
  return {
    init: function () {
      render();
      $('window').on('resize', function (e) {
        render();
      });
        //action
        google.visualization.events.addListener(chart, 'select', selectHandler);

        function selectHandler(e) {
            var selection = chart.getSelection()[0];
            var info=data.getDistinctValues(4);
            var date = info[selection.row];

            var extData={};
            extData.actionType="getAvailabilityChart";
            extData.startDate=date;

            var witBookerNgRootScope=angular.element("#ng-app").scope();

            witBookerNgRootScope.$apply(function(){
                witBookerNgRootScope.externalData=extData;
            });
        }
    },
    clear: function () {
      removeChart();
    }
  };
}

/*
 * args.wrapper:            element that contains graph
 * args.date:               first date to show
 * args.date.year           NUMBER
 * args.date.isCurrentDate  BOOL
 * args.minValue       NUMBER
 * args.maxValue       NUMBER
 * args.coin       STRING
 *
 */
var witPlotter = function (args) {
  var current = args.current,
      element = args.wrapper;
  var text      = $('<span class="text"></span>').appendTo(args.wrapper);
  var graphContainer = $('<div></div>').appendTo(args.wrapper)[0];
  var prevMonth = text.parents('.chartContainer').find('.prevMonth');
  var nextMonth = text.parents('.chartContainer').find('.nextMonth');
  var titleMonth = text.parents('.chartContainer').find('.textMonth');
  prevMonth.show();
  nextMonth.show();
  //  METHODS
  var setTitle = function (label) {
    titleMonth.html(label);
  };
  var enablePrevButton = function () {
    prevMonth.show()
  };
  var enableNextButton = function () {
    nextMonth.show()
  };
  var disablePrevButton = function () {
    prevMonth.hide()
  };
  var disableNextButton = function () {
    nextMonth.hide()
  };
  var quitCurrent = function () {
    args.data[current].graph.clear();
  };
  var showCurrent = function () {
    if (!args.data[current].graph) {
      args.data[current].graph = graphMaker({wrapper: graphContainer, values: args.data[current].values, maxValue: args.data[current].maxValue, minValue: args.data[current].minValue, coin: args.data[current].coin});
    }
    args.data[current].graph.init();
    if (current === 0) {
      disablePrevButton();
    } else if (current === 1) {
      enablePrevButton();
    } else if (current === args.data.length - 2) {
      enableNextButton();
    } else if (current === args.data.length-1){
      disableNextButton();
    }
    setTitle(args.data[current].date.monthname+' '+args.data[current].date.year);
  };
  var showNext = function () {
    if (current !== (current.length - 1)) {
      quitCurrent();
      current += 1;
      showCurrent();
    }
  };
  var showPrevious = function () {
    if (current !== 0) {
      quitCurrent();
      current -= 1;
      showCurrent();
    }
  };
  prevMonth.on('click', showPrevious);
  nextMonth.on('click', showNext);
  showCurrent();
  return {
    getCurrent:   function () {
      return current;
    },
    getData: function () {
      return args.data;
    },
    showCurrent:  showCurrent,
    showNext:     showNext,
    showPrevious: showPrevious
  }
};

function getData(el, param, callback) {
  //loading
  $.ajax({
    url: param.url,
    data: {
        id: param.id, 
        idHabitacion: param.idHabitacion, 
        idEstablecimiento:param.idEstablecimiento 
    },
    dataType: 'json',
    success: function (res) {
      // quitar loading
      $(el).html('');
      callback(el, res);
    }
  });
}