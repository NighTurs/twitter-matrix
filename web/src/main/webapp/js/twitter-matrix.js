(function () {
    $(document).ready(function () {
        var CELL_HEIGHT = 10;
        var CELL_WIDTH = 10;
        var BCOLOR = '#000';
        var NEW_CHAR_COLOR = '#0F0';
        var FONT = '10pt Courier New';
        var WS_URL = "ws://" + location.hostname + ":61614/stomp";
        var DRAW_INTERVAL = 33;

        // application state
        st = {
            width: 0,
            height: 0,
            gridn: 0,
            gridm: 0,
            // Container of matrix cells with references to currently displayed tweets
            grid: [],
            // Displayed tweets by key
            shownTweets: new Map(),
            // Tweet key that is currently hovered over
            hoverTweetKey: null,
            // Each separate row to display tweets
            rollers: [],
            // Tweet phrases by id
            phrases: new Map(),
            // Set of phrases to filter out
            filterPhrases: new Set()
        };

        var ctxover = canvover.getContext('2d');
        var ctx = canv.getContext('2d');

        // initialize app state
        adjustToNewWindowSize();

        var drawMatrix = function () {
            // shade for older text
            ctx.fillStyle = 'rgba(0,0,0,.01)';
            ctx.fillRect(0, 0, st.width, st.height);
            // setup to type new characters
            ctx.font = FONT;

            // each roller will type new character
            for (i = 0; i < st.gridn; i++) {
                roller = st.rollers[i];
                if (roller.tweetQueue.length > 0) {
                    var cell = st.grid[i][roller.cellInd];
                    var topTweet = roller.tweetQueue[0];
                    var topTweetText = topTweet.tweetText;
                    var topTweetKey = topTweet.tweetUrl;
                    ctx.fillStyle = BCOLOR;
                    ctx.fillRect(cell.x, cell.y - CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                    ctx.fillStyle = NEW_CHAR_COLOR;
                    ctx.fillText(topTweetText[roller.curTweetPos], cell.x, cell.y);

                    // cell will no longer reference previous tweet
                    if (cell.tweetKey) {
                        tweetInfo = st.shownTweets.get(cell.tweetKey);
                        if (tweetInfo.count <= 1) {
                            // tweet lost his last reference, remove it
                            st.shownTweets.delete(cell.tweetKey);
                        } else {
                            tweetInfo.count--;
                        }
                    }
                    cell.tweetKey = topTweetKey;
                    if (st.shownTweets.has(topTweetKey)) {
                        tweetInfo = st.shownTweets.get(topTweetKey);
                        tweetInfo.count++;
                        tweetInfo.textOffset = roller.curTweetPos;
                    } else {
                        st.shownTweets.set(topTweetKey, {
                            stGridI: i,
                            stGridH: roller.cellInd,
                            url: topTweet.tweetUrl,
                            text: topTweetText,
                            textOffset: roller.curTweetPos,
                            count: 1
                        });
                    }

                    roller.curTweetPos++;
                    if (roller.curTweetPos >= topTweetText.length) {
                        roller.curTweetPos = 0;
                        roller.tweetQueue.shift();
                    }
                    roller.cellInd++;
                    if (roller.cellInd >= st.gridm) {
                        roller.cellInd = 0;
                    }
                }
            }

            // highlight hovered tweet
            ctxover.clearRect(0, 0, st.width, st.height);
            ctxover.font = FONT;
            if (st.hoverTweetKey && st.shownTweets.has(st.hoverTweetKey)) {
                tweetInfo = st.shownTweets.get(st.hoverTweetKey);
                gi = tweetInfo.stGridI;
                gh = tweetInfo.stGridH;
                for (i = 0; i <= tweetInfo.textOffset; i++) {
                    cell = st.grid[gi][gh];
                    ctxover.clearRect(cell.x, cell.y - CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                    ctxover.fillStyle = '#FFF';
                    ctxover.fillText(tweetInfo.text[i], cell.x, cell.y);
                    gh++;
                    if (gh >= st.gridm) {
                        gh = 0;
                    }
                }
            }
        };

        // When tab is inactive intervals are triggered only once per second,
        // so there is need to make up for missed calls
        function smartInterval(func, interval){
            var last = new Date() - interval;
            var now;
            var numMissed;

            (function iterate(){
                func();
                now = +new Date();
                numMissed = Math.round((now - last) / interval) - 1;
                while (numMissed--) { func(); }
                last = +new Date();
                setTimeout(iterate, interval);
            })();
        }

        // run matrix animation
        smartInterval(drawMatrix, DRAW_INTERVAL);

        function adjustToNewWindowSize() {
            var i, h;

            //adjust canvas size
            st.width = canv.width = canvover.width = window.innerWidth;
            st.height = canv.height = canvover.height = window.innerHeight;
            st.gridn = Math.floor((st.height - CELL_HEIGHT) / CELL_HEIGHT);
            st.gridm = Math.floor((st.width - CELL_WIDTH) / CELL_WIDTH) + 1;

            // black background
            ctx.fillStyle = BCOLOR;
            ctx.fillRect(0, 0, st.width, st.height);
            // transparent second layer
            ctxover.clearRect(0, 0, st.width, st.height);

            //reset hovered tweet
            st.hoverTweetKey = null;

            //reinitialize grid
            st.grid = [];
            for (i = CELL_HEIGHT; i <= st.height - CELL_HEIGHT; i += CELL_HEIGHT) {
                var row = [];
                for (h = 0; h <= st.width - CELL_WIDTH; h += CELL_WIDTH) {
                    row.push({
                        x: h,
                        y: i,
                        tweetKey: null
                    })
                }
                st.grid.push(row);
            }

            // reinitialize rollers
            st.rollers = [];
            for (i = 0; i < st.gridn; i++) {
                st.rollers.push({
                    tweetQueue: [],
                    curTweetPos: 0,
                    cellInd: 0
                })
            }

            // reinitialize shown tweets map
            shownTweets = new Map();
        }

        // Upon tweet click open it's page
        canvover.addEventListener('click', function (e) {
            var cell = mouseEventToGridCell(this, e);
            if (cell && cell.tweetKey) {
                window.open(st.shownTweets.get(cell.tweetKey).url);
            }
        });

        canvover.addEventListener('mouseout', function () {
            st.hoverTweetKey = null;
        });

        canvover.addEventListener('mousemove', function (e) {
            var cell = mouseEventToGridCell(this, e);
            st.hoverTweetKey = cell ? cell.tweetKey : null;
        });

        function mouseEventToGridCell(ref, e) {
            var clickedX = e.pageX - ref.offsetLeft;
            var clickedY = e.pageY - ref.offsetTop;
            var gridI = Math.floor(clickedY / CELL_HEIGHT);
            var gridH = Math.floor(clickedX / CELL_WIDTH);
            if (gridI < st.gridn && gridH < st.gridm) {
                return st.grid[gridI][gridH];
            } else {
                return null;
            }
        }

        function phraseId(phrase) {
            return "cb_" + phrase.replace(new RegExp(' ', 'g'), '_');
        }

        $(document).on('change', '[type=checkbox]', function (e) {
            cb = $(e.target);
            label = $(e.target).parent();
            phrase = st.phrases.get(label.attr('id')).phrase;
            if (!cb.is(':checked')) {
                st.filterPhrases.add(phrase);
            } else {
                st.filterPhrases.delete(phrase);
            }
        });

        $(window).resize(function () {
            adjustToNewWindowSize()
        });

        client = Stomp.client(WS_URL);

        client.connect({}, function () {
            client.subscribe("/topic/twitter.tweet",
                function (message) {
                    tweet = JSON.parse(message.body);
                    filter = true;
                    tweet.phrases.forEach(function (entry) {
                        filter &= st.filterPhrases.has(entry);
                    });
                    if (!filter) {
                        var row = Math.floor(Math.random() * st.rollers.length);
                        st.rollers[row].tweetQueue.push(tweet);
                    }
                },
                {priority: 9}
            );
            client.subscribe("/topic/twitter.tweet.phrases",
                function (message) {
                    msg = JSON.parse(message.body);
                    curPhrases = new Map();
                    msg.phrases.forEach(function (entry) {
                        key = phraseId(entry.phrase);
                        curPhrases.set(key, {
                            phrase: entry.phrase,
                            freqMinute: entry.stats.freqMinute
                        });
                    });
                    // remove phrases that disappeared
                    st.phrases.forEach(function (value, key) {
                        if (!curPhrases.has(key)) {
                            $('#' + key).remove();
                            st.filterPhrases.delete(value);
                        }
                    });
                    // and new phrases and update existing
                    curPhrases.forEach(function (value, key) {
                        phraseSelector = '#' + key;
                        if ($(phraseSelector).length) {
                            $(phraseSelector).contents().filter(function () {
                                    return this.nodeType == 3;
                                })
                                .last().replaceWith(value.freqMinute + ' ' + value.phrase);
                        } else {
                            $("#phrases").append('<label id="' + key + '" class="btn btn-switch active"> \
                        <input type="checkbox" checked> ' + value.freqMinute + ' ' + value.phrase + '</label>')
                        }
                    });
                    st.phrases = curPhrases;
                },
                {priority: 9}
            );
        });
    });
})();