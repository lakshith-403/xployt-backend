WITH MaxCounts AS (
    SELECT 
        MAX(activeProjectCount) + 1 AS max_active_projects,
        MAX(completedProjectCount) + 1 AS max_completed_projects
    FROM ProjectLeadInfo
)
SELECT 
    u.userId,
    ((1 - COALESCE(pli.activeProjectCount, 0) / mc.max_active_projects) * 0.6 +
     (1 - COALESCE(pli.completedProjectCount, 0) / mc.max_completed_projects) * 0.4) AS combined_score
FROM Users u
LEFT JOIN ProjectLeadInfo pli ON u.userId = pli.projectLeadId
CROSS JOIN MaxCounts mc
WHERE u.role = 'ProjectLead'
ORDER BY combined_score DESC
LIMIT 1;
