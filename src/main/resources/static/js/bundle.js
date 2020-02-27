var Main = (() => {
    const defines = {};
    const entry = [null];
    function define(name, dependencies, factory) {
        defines[name] = { dependencies, factory };
        entry[0] = name;
    }
    define("require", ["exports"], (exports) => {
        Object.defineProperty(exports, "__cjsModule", { value: true });
        Object.defineProperty(exports, "default", { value: (name) => resolve(name) });
    });
    define("d3", ["exports"], function(exports) {
        Object.defineProperty(exports, "__cjsModule", { value: true });
        Object.defineProperty(exports, "default", { value: window['d3'] });
    });
    define("DurationDensity", ["require", "exports", "d3"], function (require, exports, d3) {
        "use strict";
        Object.defineProperty(exports, "__esModule", { value: true });
        /**
         * A kernel smoother smooths a kernel function as some weighted average of its
         * neighbours. The smoothing parameter is the number of neighbours to consider;
         * the lower the value the more ragged the resulting density curve.
         *
         * @param kernel The kernel to scale
         * @param bandwidth The smoothing parameter
         * @see https://en.wikipedia.org/w/index.php?title=Kernel_density_estimation&oldid=938836395#Definition
         */
        const kernelSmoother = (kernel, bandwidth) => {
            return (x) => kernel(x / bandwidth) / bandwidth;
        };
        const kernelDensityEstimator = (kernel, X) => {
            return (V) => {
                return X.map((x) => [x, d3.mean(V, (v) => kernel(x - v))]);
            };
        };
        const epanechnikov = (u) => {
            return Math.abs(u) <= 1 ? 0.75 * (1 - u * u) : 0;
        };
        const scaleTime = (seconds) => {
            const hours = seconds / 3600;
            const days = hours / 24;
            const months = days / 30;
            const checked = document.querySelectorAll("input[name=scale]:checked");
            if (!checked.length)
                return days;
            switch (checked[0].value) {
                case "h": return hours;
                case "d": return days;
                case "m": return months;
                default: return hours;
            }
        };
        const diffTime = (d) => {
            const diffMs = d.end.getTime() - d.start.getTime();
            const diffS = diffMs / 1000;
            const diff = scaleTime(diffS);
            return diff;
        };
        const convertRow = (rawRow, _index, _columns) => {
            if (!rawRow.end)
                return null;
            if (!rawRow.start)
                return null;
            // input as epoch in ms
            return {
                start: new Date(+rawRow.start),
                end: new Date(+rawRow.end),
            };
        };
        const margin = {
            top: 30,
            right: 30,
            bottom: 30,
            left: 50,
        };
        const width = 860 - margin.left - margin.right;
        const height = 800 - margin.top - margin.bottom;
        class App {
            constructor() {
                this.observations = [];
                this.data1 = [];
                this.data2 = [];
                this.repaint = (_1, _2) => { };
                this.c1f = document.getElementById("c1from");
                this.c1t = document.getElementById("c1to");
                this.c2f = document.getElementById("c2from");
                this.c2t = document.getElementById("c2to");
                this.xmax = document.getElementById("xmax");
                this.ymax = document.getElementById("ymax");
                this.summary = document.getElementById("summary");
                this.svg = d3.select("#canvas")
                    .append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                    .attr("transform", `translate(${margin.left},${margin.top})`);
                this.state = {
                    c1from: "",
                    c1to: "",
                    c2from: "",
                    c2to: "",
                    report: "",
                    scale: "",
                    xmax: "",
                    ymax: "",
                };
                this.initForm();
                this.initCanvas();
            }
            initForm() {
                // First try to reinitialize from query parameters.
                const url = new URL(window.location.href);
                const params = url.searchParams;
                const inputsForQparams = [
                    [this.c1f, "c1from"],
                    [this.c1t, "c1to"],
                    [this.c2f, "c2from"],
                    [this.c2t, "c2to"],
                    [this.xmax, "xmax"],
                    [this.ymax, "ymax"],
                ];
                for (const [input, qParam] of inputsForQparams) {
                    const qValue = params.get(qParam);
                    if (qValue) {
                        input.value = qValue;
                    }
                }
                const qScale = params.get("scale");
                if (qScale) {
                    document.getElementById("scale" + qScale).checked = true;
                }
                const qReport = params.get("report");
                if (qReport) {
                    document.getElementById("report_" + qReport).checked = true;
                }
                // Then for any missing query parameters, set some appropriate defaults.
                if (!this.c1t.valueAsNumber) {
                    this.c1t.valueAsDate = new Date();
                }
                if (!this.c1f.valueAsNumber) {
                    const now = this.c1t.valueAsNumber;
                    if (now) {
                        const twoWeeksInMs = 1000 * 3600 * 24 * 7 * 2;
                        const then = new Date(now - twoWeeksInMs);
                        this.c1f.valueAsDate = then;
                    }
                }
                if (!this.ymax.value) {
                    // Pick an arbitrary value.
                    this.ymax.valueAsNumber = 0.2;
                }
                if (!this.xmax.value) {
                    const twoWeeksInDays = 7 * 2;
                    this.xmax.valueAsNumber = twoWeeksInDays;
                }
                for (const [input, qParam] of inputsForQparams) {
                    this.state[qParam] = input.value;
                }
                // `scale` set in either HTML or query parameter.
                this.state.scale = document.querySelector("input[name=scale]:checked").value;
                // `report` set in either HTML or query parameter.
                this.state.report = document.querySelector("input[name=report]:checked").value;
                window.history.replaceState(this.state, "initial");
                window.addEventListener("popstate", (e) => this.onpopstate(e));
                document.addEventListener("input", (e) => this.onInput(e));
            }
            initCanvas() {
                const getDomainX = () => [0, this.xmax.valueAsNumber];
                const getDomainY = () => [0, this.ymax.valueAsNumber];
                const xScale = d3
                    .scaleLinear()
                    .domain(getDomainX())
                    .range([0, width]);
                const yScale = d3
                    .scaleLinear()
                    .domain(getDomainY())
                    .range([height, 0]);
                const xAxis = d3.axisBottom(xScale);
                this.svg
                    .append("g")
                    .attr("class", "x axis")
                    .attr("transform", `translate(0,${height})`)
                    .call(xAxis);
                const yAxis = d3.axisLeft(yScale);
                this.svg
                    .append("g")
                    .attr("class", "y axis")
                    .attr("transform", `translate(0,${0})`)
                    .call(yAxis);
                this.svg
                    .append("text")
                    .attr("class", "x label")
                    .attr("text-anchor", "end")
                    .attr("x", width)
                    .attr("y", height - 6)
                    .text("Time taken");
                this.svg
                    .append("text")
                    .attr("class", "y label")
                    .attr("text-anchor", "end")
                    .attr("y", 6)
                    .attr("dy", ".75em")
                    .attr("transform", "rotate(-90)")
                    .text("Proportion");
                const kernel = kernelSmoother(epanechnikov, 7);
                // Type `Density` with defined(YCoord) and non-null assertion operator is
                // what makes datum() | line() compile.
                const lineGenerator = d3.line()
                    .curve(d3.curveBasis)
                    .x((d) => xScale(d[0]))
                    .defined((d) => !!d[1])
                    .y((d) => yScale(d[1]));
                const curve1 = this.svg
                    .append("path")
                    .attr("class", "curve curve1")
                    .attr("fill", "none");
                const curve2 = this.svg
                    .append("path")
                    .attr("class", "curve curve2")
                    .attr("fill", "none");
                this.repaint = (c1data, c2data) => {
                    const kde = kernelDensityEstimator(kernel, xScale.ticks(100));
                    const density1 = kde(c1data.map(diffTime));
                    const density2 = kde(c2data.map(diffTime));
                    xScale.domain(getDomainX()).range([0, width]);
                    yScale.domain(getDomainY()).range([height, 0]);
                    xAxis.scale(xScale);
                    yAxis.scale(yScale);
                    const dur = 500;
                    this.svg
                        .select(".x.axis")
                        .transition()
                        .duration(dur)
                        .call(xAxis);
                    this.svg
                        .select(".y.axis")
                        .transition()
                        .duration(dur)
                        .call(yAxis);
                    curve1
                        .datum(density1)
                        .transition()
                        .duration(dur)
                        .attr("d", lineGenerator);
                    curve2
                        .datum(density2)
                        .transition()
                        .duration(dur)
                        .attr("d", lineGenerator);
                };
            }
            filterData() {
                const msgs = [`Total observations: ${this.observations.length}`];
                if (this.c1f.value && this.c1t.value) {
                    const c1fd = this.c1f.valueAsDate;
                    const c1td = this.c1t.valueAsDate;
                    this.data1 = this.observations.filter((d) => d.start >= c1fd && d.start < c1td);
                    msgs.push(`In period 1: ${this.data1.length}`);
                }
                else {
                    this.data1 = [];
                }
                if (this.c2f.value && this.c2t.value) {
                    const c2fd = this.c2f.valueAsDate;
                    const c2td = this.c2t.valueAsDate;
                    this.data2 = this.observations.filter((d) => d.start >= c2fd && d.start < c2td);
                    msgs.push(`In period 2: ${this.data2.length}`);
                }
                else {
                    this.data2 = [];
                }
                if (this.observations.length) {
                    const fromDates = this.observations.map((d) => d.start);
                    fromDates.sort((a, b) => a.getTime() - b.getTime());
                    const earliest = fromDates[0];
                    const latest = fromDates[fromDates.length - 1];
                    msgs.push(`Earliest: ${earliest.toLocaleDateString()}`);
                    msgs.push(`Latest: ${latest.toLocaleDateString()}`);
                }
                this.summarize(...msgs);
            }
            stableRepaintWrapper() {
                this.repaint(this.data1, this.data2);
            }
            filterDataAndRepaint() {
                this.filterData();
                this.stableRepaintWrapper();
            }
            ondata(data) {
                this.observations = data;
                this.filterDataAndRepaint();
            }
            summarize(...messages) {
                const list = document.createElement("ul");
                for (const m of messages) {
                    const li = document.createElement("li");
                    li.innerHTML = m;
                    list.appendChild(li);
                }
                this.summary.replaceChild(list, this.summary.lastElementChild);
            }
            onpopstate(e) {
                const state = e.state;
                if (!state) {
                    return;
                }
                for (const k in state) {
                    const name = k;
                    const value = state[name];
                    if (name === "scale") {
                        const el = document.getElementById(name + value);
                        el.checked = true;
                    }
                    else if (name === "report") {
                        const el = document.getElementById(`${name}_${value}`);
                        el.checked = true;
                    }
                    else {
                        const el = document.getElementById(name);
                        el.value = value;
                    }
                }
                this.filterDataAndRepaint();
                e.stopPropagation();
            }
            loadReport(name) {
                this.summarize("Loading&hellip;");
                this.summary.hidden = false;
                d3.csv(`density/${name}.csv`, {
                    headers: new Headers({ "Content-Type": "text/csv" })
                }, convertRow)
                    .then((e) => { this.ondata(e); })
                    .catch((reason) => {
                    this.summarize(`${this.state.report}: ${reason.message}`);
                });
            }
            onInput(e) {
                const target = e.target;
                if (!(target instanceof HTMLInputElement)) {
                    return;
                }
                e.stopPropagation();
                switch (target.id) {
                    case "report_ttfa":
                    case "report_ttla":
                    case "report_ttfc":
                    case "report_ttlc":
                    case "report_ttff":
                    case "report_cycle_time":
                        this.loadReport(target.value);
                        break;
                    case "c1from":
                    case "c1to":
                    case "c2from":
                    case "c2to":
                    case "xmax":
                    case "ymax":
                        this.filterDataAndRepaint();
                        break;
                    case "scaled":
                    case "scaleh":
                    case "scalem":
                        // Time scale shifts the y-coords but never affects the axis domains
                        // or ranges. Skip the idempotent data filter.
                        this.stableRepaintWrapper();
                        break;
                    default:
                        alert(`unhandled: ${target.id}`);
                }
                const name = target.name;
                const value = target.value;
                // Skip histories for silly years in the past and future.
                const looksLikeSensibleYear = /^[12][0-9]{3}-\d\d-\d\d$/.test(value);
                if (!(name.startsWith("c") && !looksLikeSensibleYear)) {
                    this.state[name] = value;
                    const search = new URLSearchParams(document.location.search);
                    search.set(name, value);
                    window.history.pushState(this.state, `${name}:${value}`, "?" + search);
                }
            }
        }
        exports.run = () => {
            const app = new App();
            app.loadReport(app.state.report);
        };
    });
    define("Main", ["require", "exports", "DurationDensity"], function (require, exports, DurationDensity) {
        "use strict";
        Object.defineProperty(exports, "__esModule", { value: true });
        function durationDensity() {
            document.addEventListener("DOMContentLoaded", DurationDensity.run);
        }
        exports.durationDensity = durationDensity;
    });
    //# sourceMappingURL=bundle.js.map
    'marker:resolver';

    function get_define(name) {
        if (defines[name]) {
            return defines[name];
        }
        else if (defines[name + '/index']) {
            return defines[name + '/index'];
        }
        else {
            const dependencies = ['exports'];
            const factory = (exports) => {
                try {
                    Object.defineProperty(exports, "__cjsModule", { value: true });
                    Object.defineProperty(exports, "default", { value: require(name) });
                }
                catch (_a) {
                    throw Error(['module "', name, '" not found.'].join(''));
                }
            };
            return { dependencies, factory };
        }
    }
    const instances = {};
    function resolve(name) {
        if (instances[name]) {
            return instances[name];
        }
        if (name === 'exports') {
            return {};
        }
        const define = get_define(name);
        instances[name] = {};
        const dependencies = define.dependencies.map(name => resolve(name));
        define.factory(...dependencies);
        const exports = dependencies[define.dependencies.indexOf('exports')];
        instances[name] = (exports['__cjsModule']) ? exports.default : exports;
        return instances[name];
    }
    if (entry[0] !== null) {
        return resolve(entry[0]);
    }
})();