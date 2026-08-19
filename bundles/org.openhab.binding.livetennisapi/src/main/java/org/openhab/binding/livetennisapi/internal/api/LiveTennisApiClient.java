Two edits to this file (both applied and built):

(1) @author tag rename (line 46):
  OLD:  * @author Ben Synapse - Initial contribution
  NEW:  * @author Ben Abulafia - Initial contribution

(2) getLiveMatches() javadoc — no longer claims the snapshot is always complete. Replace the block immediately above `public List<Match> getLiveMatches()`:
  OLD:
    /**
     * Returns all matches currently in progress, with their latest score. The endpoint is paginated: this reads the
     * {@code meta.has_more} flag and pages forward until the snapshot is complete (or the page cap is reached), so
     * matches beyond the first page are not silently dropped.
     */
  NEW:
    /**
     * Returns the matches currently in progress, with their latest score. The endpoint is paginated: this reads the
     * {@code meta.has_more} flag and pages forward, so matches beyond the first page are not silently dropped. Paging
     * stops at {@link #MAX_LIVE_PAGES}; if the API still reports more matches at that cap the returned snapshot is
     * deliberately truncated rather than complete, and that truncation is logged as a warning by
     * {@link #collectLiveMatches}.
     */

No executable code changed in this file; collectLiveMatches() already logged the truncation warning.