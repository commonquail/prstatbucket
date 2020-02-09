<!DOCTYPE html>
<html lang="en">
<head>
    <#assign appjs = "/js/bundle.js">
    <#assign appcss = "/css/duration-density.css">
    <meta charset="UTF-8">
    <title>Duration Density Estimates</title>
    <script src="https://d3js.org/d3.v5.min.js"></script>
    <script src="${appjs}"></script>
    <script>Main.durationDensity()</script>
    <link rel="stylesheet" href="${appcss}">
</head>
<body>
    <h1>Density estimate report of durations</h1>
    <div class="main-grid">
        <div id="canvas" class="main-grid--left"></div>

        <div class="main-grid--controls">
            <h2>Controls</h2>
            <div class="controls--ctl">
                <h3 class="ctl-grid--span">Report</h3>

                <div class="ctl-grid--span">
                    <input type="radio" id="report_cycle_time" value="cycle_time" name="report" checked>
                    <label for="report_cycle_time">Cycle time</label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="report_ttff" value="ttff" name="report">
                    <label for="report_ttff"><abbr title="Time To First Feedback">TTFF</abbr></label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="report_ttfa" value="ttfa" name="report">
                    <label for="report_ttfa"><abbr title="Time To First Approval">TTFA</abbr></label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="report_ttla" value="ttla" name="report">
                    <label for="report_ttla"><abbr title="Time To Last Approval">TTLA</abbr></label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="report_ttfc" value="ttfc" name="report">
                    <label for="report_ttfc"><abbr title="Time To First Comment">TTFC</abbr></label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="report_ttlc" value="ttlc" name="report">
                    <label for="report_ttlc"><abbr title="Time To Last Comment">TTLC</abbr></label>
                </div>
            </div>

            <div class="controls--ctl ctl-grid">
                <h3 class="ctl-grid--span">Axis bounds</h3>

                <label class="ctl-grid--left" for="ymax"><var>Y</var> max</label>
                <input class="ctl-grid--right" name="ymax" type="number" min="0" max="1" step="0.01" id="ymax">

                <label class="ctl-grid--left" for="xmax"><var>X</var> max</label>
                <input class="ctl-grid--right" name="xmax" type="number" min="0" step="1" id="xmax">
            </div>

            <div class="controls--ctl">
                <h3 class="ctl-grid--span"><var>X</var> axis time scale</h3>

                <div class="ctl-grid--span">
                    <input type="radio" id="scaleh" value="h" name="scale">
                    <label for="scaleh">Hours</label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="scaled" value="d" name="scale" checked>
                    <label for="scaled">Days</label>
                </div>
                <div class="ctl-grid--span">
                    <input type="radio" id="scalem" value="m" name="scale">
                    <label for="scalem">Months</label>
                </div>
            </div>

            <div class="controls--ctl ctl-grid">
                <h3 class="curve-legend curve1 ctl-grid--span">Period 1</h3>
                <label class="ctl-grid--left" for="c1from">From</label>
                <input class="ctl-grid--right" type="date" name="c1from" id="c1from">
                <label class="ctl-grid--left" for="c1to">Until</label>
                <input class="ctl-grid--right" type="date" name="c1to" id="c1to">
            </div>

            <div class="controls--ctl ctl-grid">
                <h3 class="curve-legend curve2 ctl-grid--span">Period 2</h3>
                <label class="ctl-grid--left" for="c2from">From</label>
                <input class="ctl-grid--right" type="date" name="c2from" id="c2from">
                <label class="ctl-grid--left" for="c2to">Until</label>
                <input class="ctl-grid--right" type="date" name="c2to" id="c2to">
            </div>

            <div id="summary" class="controls--ctl" hidden>
                <h3 class="ctl-grid--span">Summary</h3>
                <ul></ul>
            </div>
        </div>
    </div>
    <h2>Reports</h2>
    <p>
     All the reports are the measure of time from a pull request is created
     until some event transpires.
    </p>
    <p>
     Measures are all in total time, with no notion of &ldquo;off-work&rdquo;
     hours. This skews measures towards longer-than-actual times but is
     preferable to accidentally skewing the other way.
    </p>
    <dl>
    <dt><dfn>Cycle time</dfn></dt>
    <dd>
     The time from a pull request is created until it is either merged or
     declined. Short cycle time means an individual unit of work moved fast.
     Very short cycle time may mean that work was sloppy.
    </dd>
    <dt><abbr>TTFF</abbr>: <dfn>Time To First Feedback</dfn></dt>
    <dd>
     The time until some user other than the pull request author performs an
     activity that the author can respond to. This represents &ldquo;the time
     until the author can proceed&rdquo;. It is the lower of TTFA and TTFC and
     is more useful than either of those for this specific purpose.
    </dd>
    <dt><abbr>TTFA</abbr>: <dfn>Time To First Approval</dfn></dt><dd>
     The time until the first approval by some user other than the pull request
     author. Short TTFA implies opportunity for short cycle time. Short TTFA
     but long cycle time implies obstacles to merging, such as timing or
     insufficient review by reviewers.
    </dd>
    <dt><abbr>TTLA</abbr>: <dfn>Time To Last Approval</dfn></dt>
    <dd>
     The time until the last approval by some user other than the pull request
     author. A large gap between TTFA and TTLA may mean that cycle time is
     dominated by certain reviewers.
    </dd>
    <dt><abbr>TTFC</abbr>: <dfn>Time To First Comment</dfn></dt>
    <dd>
     The time until the first comment by some user other than the pull request
     author.
    </dd>
    <dt><abbr>TTLC</abbr>: <dfn>Time To Last Comment</dfn></dt>
    <dd>
     The time until the last comment by some user other than the pull request
     author.
    </dd>
    </dl>
</body>
</html>
