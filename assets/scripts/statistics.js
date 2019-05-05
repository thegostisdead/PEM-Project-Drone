class Statistics {

    static initialize() {
        Statistics.DIVS = {
            FLIGHTS: document.getElementById("statistics-flights"),
            GRAPHS: document.getElementById("statistics-graphs")
        }

        Statistics.loadedPhysicalQualities = null;
        Statistics.maxValueCount = -1;
        Statistics.currentFlightCharts = null;

        Statistics.loadPhysicalQualities();
    }

    static loadPhysicalQualities() {
        DroneApi.call(ENDPOINT_QUALITIES_LIST, function(json, success) {
            if (!success) {
                log("Failed to loaded physical qualities.");
                return true;
            }

            let loaded = {};
            for (let quality of json.loaded) {
                let name = quality.name;
                let unit = quality.unit;
                let useGraph = quality.useGraph;

                console.log("Statistics: Added physical quality \"" + name + "\" (in " + unit + ", " + (useGraph ? "" : "not ") + "using graph).");

                loaded[name] = {
                    name: name,
                    unit: unit,
                    useGraph: useGraph
                };
            }

            Statistics.loadedPhysicalQualities = loaded;
            Statistics.maxValueCount = json.max_request_size;

            Statistics.subscribeToSocket();
            Statistics.fillStatistics();
        });
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.starting", "flight.finished"], function(identifier, json) {
            Statistics.fillStatistics();
        });

        DroneSocket.subscribe(["qualities.new"], function(identifier, json) {
            Statistics.updateCurrentFlightGraphs(json);
        });
    }

    static fillStatistics() {
        Statistics.DIVS.FLIGHTS.innerHTML = HTML_PART_LOADER;

        DroneApi.call(ENDPOINT_FLIGHTS, function(json, success) {
            if (!success) {
                log("Failed to fetch flights.");
                return true;
            }

            let firstIsCurrent = json.current.active;
            let flights = [];

            if (firstIsCurrent) {
                let currentFlight = json.current.flight;

                flights.push(currentFlight);
            }
            flights.extend(json.all);

            let html = "";
            for (let index = 0; index < flights.length; index++) {
                let flight = flights[index];

                let active = index == 0 && firstIsCurrent;

                html += "<a data-current=\"" + active + "\" data-flight=\"" + flight.local_file + "\" onclick=\"Statistics.selectFlight(this);\" href=\"#\" class=\"statistics-flight-item list-group-item list-group-item-action\">";
                html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
                html += "		<h5 class=\"mb-1\">" + flight.name + "</h5>\n";
                html += "		<small>" + new Date(flight.start).toLocaleString() + "</small>\n";
                html += "	</div>\n";
                html += "</a>\n";
            }

            Statistics.DIVS.FLIGHTS.innerHTML = html;

            let elements = Statistics.DIVS.FLIGHTS.getElementsByClassName("statistics-flight-item");

            if (elements.length > 0) {
                Statistics.selectFlight(elements[0]);
            }
        });
    }

    static unselectAllFlights() {
        let elements = Statistics.DIVS.FLIGHTS.getElementsByClassName("statistics-flight-item");

        for (let element of elements) {
            element.classList.remove("active");
        }
    }

    static selectFlight(div) {
        Statistics.unselectAllFlights();
        Statistics.DIVS.GRAPHS.innerHTML = HTML_PART_LOADER;

        let flightLocalFileName = div.dataset.flight;
        let isCurrent = div.dataset.current.toBoolean();
        let endpoint = isCurrent ? ENDPOINT_QUALITIES_DATA_CURRENT : ENDPOINT_QUALITIES_DATA_ALL;

        DroneApi.call(endpoint, function(json, success) {
            if (!success) {
                log("Failed to fetch qualities data.");
                return true;
            }

            Statistics.unselectAllFlights(); /* Just to be sure */

            div.classList.add("active");

            let qualities = isCurrent ? json.flight : json.flights.all[flightLocalFileName];

            let forEach = function(callback) {
                let loaded = Statistics.loadedPhysicalQualities;
                for (let quality in loaded) {
                    quality = loaded[quality];
                    let name = quality.name;
                    let valueHolders = qualities[name];

                    if (quality.useGraph) {
                        callback(quality, name, valueHolders);
                    }
                }
            };

            let html = "";
            forEach(function(quality, name, valueHolders) {
                let unit = quality.unit;

                let i18nKey = "quality." + name;

                html += "<div class=\"chart-container\" style=\"width:400px; height:200px; float:left; display: inline-block;\">";
                html += "	<h2 class=\"translatable chart-title\" data-i18n=\"" + i18nKey + "\">" + i18n.get(i18nKey) + "</h2><h6>(" + unit + ")</h6>";
                html += "	<canvas id=\"statistics-chart-" + name + "\"></canvas>";
                html += "</div>";
            });
            Statistics.DIVS.GRAPHS.innerHTML = html;

            Statistics.currentFlightCharts = isCurrent ? {} : null;
            forEach(function(quality, name, valueHolders) {
                let labels = [];
                let values = [];

                for (let valueHolder of valueHolders) {
                    labels.unshift(new Date(valueHolder.date).toSimpleHourString());
                    values.unshift(valueHolder.content);
                }

                let chart = new Chart(document.getElementById("statistics-chart-" + name), {
                    type: "line",
                    data: {
                        labels: labels,
                        datasets: [{
                            label: name,
                            data: values,
                            fill: false,
                            borderColor: "rgb(84, 110, 122)",
                            lineTension: 0.1
                        }]
                    },
                    options: {
                        legend: {
                            display: false
                        },
                        tooltips: {
                            callbacks: {
                                label: function(tooltipItem, data) {
                                    let quality = data.datasets[0].label;

                                    return i18n.get("quality." + quality) + ": " + tooltipItem.yLabel + " " + Statistics.loadedPhysicalQualities[quality].unit;
                                }
                            }
                        },
                        scales: {
                            yAxes: [{
                                ticks: {
                                    suggestedMin: 0,
                                    suggestedMax: 50
                                }
                            }]
                        }
                    }
                });

                if (isCurrent && Statistics.currentFlightCharts != null) {
                    Statistics.currentFlightCharts[name] = chart;
                }
            });
        });
    }

    static updateCurrentFlightGraphs(data) {
        if (Statistics.currentFlightCharts == null) {
            return;
        }

        let newValues = data.new_values;
        let maxValueSize = Statistics.maxValueCount;

        let charts = Statistics.currentFlightCharts;
        for (let quality in Statistics.loadedPhysicalQualities) {
            let chart = charts[quality];

            let values = newValues[quality];

            if (chart == null || values == null) {
                continue;
            }

            for (let valueHolder of values) {
                let labels = chart.data.labels;
                let chartData = chart.data.datasets[0].data;

                labels.unshift(new Date(valueHolder.date).toSimpleHourString());
                chartData.unshift(valueHolder.content);

                if (maxValueSize != -1) {
                    while (labels.length > maxValueSize) {
                        labels.pop();
                        chartData.pop();
                    }
                }
            }

            chart.update();
        }
    }

    static display(state) {
        let modal = document.getElementById("modal-statistics");
        let display = "none";

        if (state === true) {
            display = "block";
        }

        modal.style.display = display;
    }

}